package com.typesafe.config.impl

import java.util.Properties

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import helloscala.common.Configuration

object ConfigurationHelper {
  def fromProperties(props: Properties): Configuration = {
    ConfigFactory.systemProperties()
    val config =
      Parseable
        .newProperties(props, ConfigParseOptions.defaults())
        .parse()
        .asInstanceOf[AbstractConfigObject]
        .toConfig
    Configuration(config)
  }
}
