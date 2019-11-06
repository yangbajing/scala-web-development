package helloscala.common.util

import java.nio.file.Path
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.util.Properties
import java.util.function.Consumer

import com.typesafe.config._
import com.typesafe.config.impl.ConfigurationHelper
import helloscala.common.exception.HSException
import helloscala.common.types.ObjectId

import scala.jdk.CollectionConverters._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.control.NonFatal

case class Configuration(underlying: Config) {

  // TODO 基于 etcd 或 consul 进行重设
  //  underlying.withFallback()

  /**
   * 合并两个HlConfiguration
   */
  def ++(other: Configuration): Configuration =
    Configuration(other.underlying.withFallback(underlying))

  /**
   * Reads a value from the underlying implementation.
   * If the value is not set this will return None, otherwise returns Some.
   *
   * Does not check neither for incorrect type nor null value, but catches and wraps the error.
   */
  private def readValue[T](path: String, v: => T): Option[T] =
    try {
      if (underlying.hasPathOrNull(path)) Some(v) else None
    } catch {
      case NonFatal(e) =>
        throw new IllegalArgumentException(path + e.getMessage, e)
    }

  /**
   * Check if the given path exists.
   */
  def has(path: String): Boolean = underlying.hasPath(path)

  def getConfiguration(path: String): Configuration = get[Configuration](path)

  def getConfig(path: String): Config = get[Config](path)

  def getArray(path: String): Array[String] = get[Array[String]](path)

  def getString(path: String): String = get[String](path)

  def getBytes(path: String): Long = underlying.getBytes(path)

  def getInt(s: String): Int = underlying.getInt(s)

  def getDuration(path: String): java.time.Duration =
    underlying.getDuration(path)

  def getOptionString(path: String): Option[String] = get[Option[String]](path)

  def getProperties(path: String): Properties = get[Properties](path)

  def getMap(path: String): Map[String, String] = get[Map[String, String]](path)

  def getJavaMap(path: String): java.util.Map[String, String] =
    get[java.util.Map[String, String]](path)

  /**
   * Get the config at the given path.
   */
  def get[A](path: String)(implicit loader: ConfigLoader[A]): A =
    loader.load(underlying, path)

  def getOrElse[A](path: String, deft: => A)(implicit loader: ConfigLoader[A]): A = get[Option[A]](path).getOrElse(deft)

  /**
   * Get the config at the given path and validate against a set of valid values.
   */
  def getAndValidate[A](path: String, values: Set[A])(implicit loader: ConfigLoader[A]): A = {
    val value = get(path)
    if (!values(value)) {
      throw reportError(path, s"Incorrect value, one of (${values.mkString(", ")}) was expected.")
    }
    value
  }

  /**
   * Get a value that may either not exist or be null. Note that this is not generally considered idiomatic Config
   * usage. Instead you should define all config keys in a reference.conf file.
   */
  def getOptional[A](path: String)(implicit loader: ConfigLoader[A]): Option[A] =
    readValue(path, get[A](path))

  /**
   * Retrieves a configuration value as `Milliseconds`.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * val timeout = configuration.getMillis("engine.timeout")
   * }}}
   *
   * The configuration must be provided as:
   *
   * {{{
   * engine.timeout = 1 second
   * }}}
   */
  def getMillis(path: String): Long = get[Duration](path).toMillis

  /**
   * Retrieves a configuration value as `Milliseconds`.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * val timeout = configuration.getNanos("engine.timeout")
   * }}}
   *
   * The configuration must be provided as:
   *
   * {{{
   * engine.timeout = 1 second
   * }}}
   */
  def getNanos(path: String): Long = get[Duration](path).toNanos

  /**
   * Returns available keys.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * val keys = configuration.keys
   * }}}
   *
   * @return the set of keys available in this configuration
   */
  def keys: Set[String] = underlying.entrySet.asScala.map(_.getKey).toSet

  /**
   * Returns sub-keys.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * val subKeys = configuration.subKeys
   * }}}
   *
   * @return the set of direct sub-keys available in this configuration
   */
  def subKeys: Set[String] = underlying.root().keySet().asScala.toSet

  /**
   * Returns every path as a set of key to value pairs, by recursively iterating through the
   * config objects.
   */
  def entrySet: Set[(String, ConfigValue)] =
    underlying.entrySet().asScala.map(e => e.getKey -> e.getValue).toSet

  /**
   * Creates a configuration error for a specific configuration key.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * throw configuration.reportError("engine.connectionUrl", "Cannot connect!")
   * }}}
   *
   * @param path    the configuration key, related to this error
   * @param message the error message
   * @param e       the related exception
   * @return a configuration exception
   */
  def reportError(path: String, message: String, e: Option[Throwable] = None): HSException = {
    val origin = Option(
      if (underlying.hasPath(path)) underlying.getValue(path).origin
      else underlying.root.origin)
    Configuration.configError(message, origin, e)
  }

  /**
   * Creates a configuration error for this configuration.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * throw configuration.globalError("Missing configuration key: [yop.url]")
   * }}}
   *
   * @param message the error message
   * @param e       the related exception
   * @return a configuration exception
   */
  def globalError(message: String, e: Option[Throwable] = None): HSException =
    Configuration.configError(message, Option(underlying.root.origin), e)
}

object Configuration {

  def configError(message: String, origin: Option[ConfigOrigin], me: Option[Throwable]): HSException = {
    val msg = origin.map(o => s"[$o] $message").getOrElse(message)
    me.map(e => new HSException(ErrCodes.UNKNOWN, msg, e)).getOrElse(new HSException(ErrCodes.UNKNOWN, msg, null))
  }

  def apply(): Configuration = Configuration(ConfigFactory.load())

  def apply(props: Properties): Configuration =
    ConfigurationHelper.fromProperties(props)

  def parseString(content: String): Configuration =
    Configuration(ConfigFactory.parseString(content))

}

/**
 * A config loader
 */
trait ConfigLoader[A] {
  self =>
  def load(config: Config, path: String = ""): A

  def map[B](f: A => B): ConfigLoader[B] = new ConfigLoader[B] {

    override def load(config: Config, path: String): B =
      f(self.load(config, path))
  }
}

object ConfigLoader {

  def apply[A](f: Config => String => A): ConfigLoader[A] =
    new ConfigLoader[A] {

      override def load(config: Config, path: String): A =
        f(config)(path)
    }

  implicit val stringLoader: ConfigLoader[String] = ConfigLoader(_.getString)

  implicit val objectIdLoader: ConfigLoader[ObjectId] =
    stringLoader.map(ObjectId.apply)

  implicit val seqStringLoader: ConfigLoader[Seq[String]] =
    ConfigLoader(_.getStringList).map(_.asScala.toVector)

  implicit val seqObjectIdLoader: ConfigLoader[Seq[ObjectId]] =
    seqStringLoader.map(_.map(ObjectId.apply))

  implicit val arrayStringLoader: ConfigLoader[Array[String]] =
    ConfigLoader(_.getStringList).map { list =>
      val arr = new Array[String](list.size())
      list.toArray(arr)
      arr
    }

  implicit val intLoader: ConfigLoader[Int] = ConfigLoader(_.getInt)

  implicit val seqIntLoader: ConfigLoader[Seq[Int]] =
    ConfigLoader(_.getIntList).map(_.asScala.map(_.toInt).toVector)

  implicit val booleanLoader: ConfigLoader[Boolean] = ConfigLoader(_.getBoolean)

  implicit val seqBooleanLoader: ConfigLoader[Seq[Boolean]] =
    ConfigLoader(_.getBooleanList).map(_.asScala.map(_.booleanValue).toVector)

  implicit val durationLoader: ConfigLoader[Duration] = ConfigLoader { config => path =>
    if (!config.getIsNull(path)) config.getDuration(path).toNanos.nanos
    else Duration.Inf
  }

  // Note: this does not support null values but it added for convenience
  implicit val seqDurationLoader: ConfigLoader[Seq[Duration]] =
    ConfigLoader(_.getDurationList).map(_.asScala.map(_.toNanos.nanos).toVector)

  implicit val finiteDurationLoader: ConfigLoader[FiniteDuration] =
    ConfigLoader(_.getDuration).map(_.toNanos.nanos)

  implicit val seqFiniteDurationLoader: ConfigLoader[Seq[FiniteDuration]] =
    ConfigLoader(_.getDurationList).map(_.asScala.map(_.toNanos.nanos).toVector)

  implicit val doubleLoader: ConfigLoader[Double] = ConfigLoader(_.getDouble)

  implicit val seqDoubleLoader: ConfigLoader[Seq[Double]] =
    ConfigLoader(_.getDoubleList).map(_.asScala.map(_.doubleValue).toVector)

  implicit val numberLoader: ConfigLoader[Number] = ConfigLoader(_.getNumber)

  implicit val seqNumberLoader: ConfigLoader[Seq[Number]] =
    ConfigLoader(_.getNumberList).map(_.asScala.toVector)

  implicit val longLoader: ConfigLoader[Long] = ConfigLoader(_.getLong)

  implicit val seqLongLoader: ConfigLoader[Seq[Long]] =
    ConfigLoader(_.getDoubleList).map(_.asScala.map(_.longValue).toVector)

  implicit val bytesLoader: ConfigLoader[ConfigMemorySize] = ConfigLoader(_.getMemorySize)

  implicit val seqBytesLoader: ConfigLoader[Seq[ConfigMemorySize]] =
    ConfigLoader(_.getMemorySizeList).map(_.asScala.toVector)

  implicit val configLoader: ConfigLoader[Config] = ConfigLoader(_.getConfig)
  implicit val configListLoader: ConfigLoader[ConfigList] = ConfigLoader(_.getList)
  implicit val configObjectLoader: ConfigLoader[ConfigObject] = ConfigLoader(_.getObject)

  implicit val seqConfigLoader: ConfigLoader[Seq[Config]] =
    ConfigLoader(_.getConfigList).map(_.asScala.toVector)

  implicit val configurationLoader: ConfigLoader[Configuration] =
    configLoader.map(Configuration(_))

  implicit val seqConfigurationLoader: ConfigLoader[Seq[Configuration]] =
    seqConfigLoader.map(_.map(Configuration(_)))

  /**
   * Loads a value, interpreting a null value as None and any other value as Some(value).
   */
  implicit def optionLoader[A](implicit valueLoader: ConfigLoader[A]): ConfigLoader[Option[A]] =
    new ConfigLoader[Option[A]] {

      override def load(config: Config, path: String): Option[A] =
        if (!config.hasPath(path) || config.getIsNull(path)) None
        else {
          val value = valueLoader.load(config, path)
          Some(value)
        }
    }

  implicit val propertiesLoader: ConfigLoader[Properties] =
    new ConfigLoader[Properties] {

      def make(props: Properties, parentKeys: String, obj: ConfigObject): Unit =
        obj
          .keySet()
          .forEach(new Consumer[String] {

            override def accept(key: String): Unit = {
              val value = obj.get(key)
              val propKey =
                if (StringUtils.isNoneBlank(parentKeys)) parentKeys + "." + key
                else key
              value.valueType() match {
                case ConfigValueType.OBJECT =>
                  make(props, propKey, value.asInstanceOf[ConfigObject])
                case _ =>
                  props.put(propKey, value.unwrapped())
              }
            }
          })

      override def load(config: Config, path: String): Properties = {
        val obj =
          if (StringUtils.isBlank(path)) config.root()
          else config.getObject(path)
        val props = new Properties()
        make(props, "", obj)
        props
      }
    }

  implicit val scalaMapLoader: ConfigLoader[Map[String, String]] =
    new ConfigLoader[Map[String, String]] {

      def make(props: mutable.Map[String, String], parentKeys: String, obj: ConfigObject): Unit =
        obj
          .keySet()
          .forEach(new Consumer[String] {

            override def accept(key: String): Unit = {
              val value = obj.get(key)
              val propKey =
                if (StringUtils.isNoneBlank(parentKeys)) parentKeys + "." + key
                else key
              value.valueType() match {
                case ConfigValueType.OBJECT =>
                  make(props, propKey, value.asInstanceOf[ConfigObject])
                case _ =>
                  props.put(propKey, value.unwrapped().toString)
              }
            }
          })

      override def load(config: Config, path: String): Map[String, String] = {
        val obj =
          if (StringUtils.isBlank(path)) config.root()
          else config.getObject(path)
        val props = mutable.Map[String, String]()
        make(props, "", obj)
        props.toMap
      }
    }

  implicit val javaMapLoader: ConfigLoader[java.util.Map[String, String]] =
    scalaMapLoader.map(v => v.asJava)

  implicit val pathLoader: ConfigLoader[Path] =
    stringLoader.map(str => Paths.get(str))

  implicit val seqPathLoader: ConfigLoader[Seq[Path]] =
    seqStringLoader.map(strs => strs.map(str => Paths.get(str)))

  implicit val offsetDateTimeLoader: ConfigLoader[OffsetDateTime] =
    stringLoader.map { str =>
      TimeUtils.toOffsetDateTime(str)
    }

  //  implicit def mapLoader[A](implicit valueLoader: ConfigLoader[A]): ConfigLoader[Map[String, A]] =
  //    new ConfigLoader[Map[String, A]] {
  //      override def load(config: Config, path: String): Map[String, A] = {
  //        val obj = config.getObject(path)
  //        val conf = obj.toConfig
  //        obj.keySet().asScala.map { key =>
  //          key -> valueLoader.load(conf, key)
  //        }.toMap
  //      }
  //    }
}
