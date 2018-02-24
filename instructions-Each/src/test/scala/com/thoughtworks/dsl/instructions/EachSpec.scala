package com.thoughtworks.dsl.instructions

import org.scalatest.{FreeSpec, Matchers}
import Each.fork

/**
  * @author 杨博 (Yang Bo)
  */
class EachSpec extends FreeSpec with Matchers {
  type AsyncFunction[Domain, +A] = (A => Domain) => Domain

  "nested" - {
    "each" - {

      "val" in {
        val seq = 1 to 10

        def run(): Seq[Int] = Seq {
          val plus100 = Seq {
            !Each(seq) + 100
          }
          plus100.length should be(1)
          !Each(plus100)
        }

        val result = run()
        result.length should be(10)
        result.last should be(110)
      }
      "def" in {
        val seq = 1 to 10

        def run(): Seq[Int] = Seq {
          def plus100 = Seq {
            !Each(seq) + 100
          }
          plus100.length should be(10)
          !Each(plus100)
        }

        val result = run()
        result.length should be(10)
        result.last should be(110)
      }
    }

    "foreach" - {

      "val" in {
        val seq = 1 to 10

        def run(): Unit = {
          val plus100 = Seq {
            !Each(seq) + 100
          }
          plus100.length should be(1)
          !Each(plus100)
        }

        run()
      }
      "def" in {
        val seq = 1 to 10

        def run(): Unit = {
          def plus100 = Seq {
            !Each(seq) + 100
          }
          plus100.length should be(10)
          !Each(plus100)
        }

        run()
      }
    }
  }

  "foreach" in {
    val seq = 1 to 10
    var accumulator = 0
    def loop(): Unit = {
      accumulator += !Each(seq)
    }
    loop()
    accumulator should be(55)
  }

  "default parameter" in {

    def foo(s: Seq[Int] = Seq {
      !fork(1, 2, 3) + 100
    }) = s

    foo() should be(Seq(101, 102, 103))

  }

  "val in class" in {
    class C {
      val ascii: Set[Int] = Set(
        !Each(Seq(1, 2, 3, 2)) + 100
      )
    }

    (new C).ascii should be(Set(101, 102, 103))
  }

  "Given a continuation that uses Yield and Each expressions" - {

    def asyncFunction: AsyncFunction[Stream[String], Unit] = _ {
      !Yield("Entering asyncFunction")
      val subThreadId: Int = !fork(0, 1)
      !Yield(s"Fork sub-thread $subThreadId")
      !Yield("Leaving asyncFunction")
    }

    "When create a generator that contains Yield, Shift, and Each expressions" - {

      def generator: Stream[String] = {
        !Yield("Entering generator")
        val threadId = !fork(0, 1)
        !Yield(s"Fork thread $threadId")
        !Shift(asyncFunction)
        Stream("Leaving generator")
      }

      "Then the generator should contains yield values" in {
        generator should be(
          Seq(
            /**/ "Entering generator",
            /****/ "Fork thread 0",
            /******/ "Entering asyncFunction",
            /********/ "Fork sub-thread 0",
            /**********/ "Leaving asyncFunction",
            /**********/ "Leaving generator",
            /********/ "Fork sub-thread 1",
            /**********/ "Leaving asyncFunction",
            /**********/ "Leaving generator",
            /****/ "Fork thread 1",
            /******/ "Entering asyncFunction",
            /********/ "Fork sub-thread 0",
            /**********/ "Leaving asyncFunction",
            /**********/ "Leaving generator",
            /********/ "Fork sub-thread 1",
            /**********/ "Leaving asyncFunction",
            /**********/ "Leaving generator"
          ))
      }

    }

  }

}
