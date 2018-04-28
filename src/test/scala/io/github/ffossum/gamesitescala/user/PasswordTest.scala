package io.github.ffossum.gamesitescala.user

import org.scalatest.prop.Checkers
import org.scalatest.{FunSuite, Matchers}
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._

class PasswordTest extends FunSuite with Matchers with Checkers {

  test("hashed password is valid") {
    check { (rawPassword: String) =>
      val pwd = Password(rawPassword)
      PasswordHash.checkPassword(pwd, pwd.hash)
    }
  }

  test("invalid hash format results in false") {
    PasswordHash.checkPassword(Password("hunter2"), PasswordHash("invalid")) shouldBe false
  }
}
