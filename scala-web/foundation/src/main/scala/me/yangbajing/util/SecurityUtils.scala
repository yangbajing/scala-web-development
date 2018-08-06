package me.yangbajing.util

import java.security.{MessageDigest, SecureRandom}
import java.util


case class SaltPassword(salt: Array[Byte], saltPwd: Array[Byte])

/**
  * Created by yangbajing(yangbajing@gmail.com) on 2017-03-27.
  */
object SecurityUtils {

  /**
    * 生成通用 Salt 及 Salt Password
    * @param password 待生成密码
    * @return
    */
  def generatePassword(password: String): SaltPassword = {
    val random = SecureRandom.getInstanceStrong
    val salt = new Array[Byte](8)
    random.nextBytes(salt)
    val md = MessageDigest.getInstance("SHA1")
    md.digest(salt ++ password.getBytes)
    val saltPwd = md.digest()
    SaltPassword(salt, saltPwd)
  }

  /**
    * 校验密码
    *
    * @param salt     salt
    * @param saltPwd  salt password
    * @param password request password
    * @return
    */
  def matchSaltPassword(salt: Array[Byte], saltPwd: Array[Byte], password: Array[Byte]): Boolean = {
    val md = MessageDigest.getInstance("SHA1")
    md.digest(salt ++ password)
    val bytes = md.digest()
    util.Arrays.equals(saltPwd, bytes)
  }

}
