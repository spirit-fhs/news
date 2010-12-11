/**
 * Copyright (c) 2010 spirit-fhs
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of his contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHORS ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.unsane.spirit.news

import org.specs.Specification
import model.Config

object NewsSpec extends Specification with Config {

  "Spirit News" should {
    "loadEmails config correctly" in {
      val I = loadEmails(Array("I1", "I2", "I3", "I4", "I5", "I6"))
      val WI = loadEmails(Array("WI1", "WI2", "WI3", "WI4", "WI5", "WI6"))
      val MM = loadEmails(Array("MM6", "MM5", "MM4", "MM3", "MM2", "MM1"))
      val IS = loadEmails(Array("IS1", "IS2", "IS3", "IS4", "IS5", "IS6"))
      val MAI = loadEmails(Array("MAI1", "MAI2", "MAI3", "MAI4"))
      val others = loadEmails(Array("semester", "alte_semester", "alte_i", "alte_wi"))

      others(0).mustEqual("semester@informatik.fh-schmalkalden.de")
      others(1).mustEqual("alte_semester@informatik.fh-schmalkalden.de")
      others(2).mustEqual("alte_i@informatik.fh-schmalkalden.de")
      others(3).mustEqual("alte_wi@informatik.fh-schmalkalden.de")
      
      I(0).mustEqual("I1@informatik.fh-schmalkalden.de")
      I(1).mustEqual("I2@informatik.fh-schmalkalden.de")
      I(2).mustEqual("I3@informatik.fh-schmalkalden.de")
      I(3).mustEqual("I4@informatik.fh-schmalkalden.de")
      I(4).mustEqual("I5@informatik.fh-schmalkalden.de")
      I(5).mustEqual("I6@informatik.fh-schmalkalden.de")

      WI(5).mustEqual("WI6@informatik.fh-schmalkalden.de")
      WI(4).mustEqual("WI5@informatik.fh-schmalkalden.de")
      WI(3).mustEqual("WI4@informatik.fh-schmalkalden.de")
      WI(2).mustEqual("WI3@informatik.fh-schmalkalden.de")
      WI(1).mustEqual("WI2@informatik.fh-schmalkalden.de")
      WI(0).mustEqual("WI1@informatik.fh-schmalkalden.de")

      MM(5).mustEqual("MM1@informatik.fh-schmalkalden.de")
      MM(4).mustEqual("MM2@informatik.fh-schmalkalden.de")
      MM(3).mustEqual("MM3@informatik.fh-schmalkalden.de")
      MM(2).mustEqual("MM4@informatik.fh-schmalkalden.de")
      MM(1).mustEqual("MM5@informatik.fh-schmalkalden.de")
      MM(0).mustEqual("MM6@informatik.fh-schmalkalden.de")

      IS(5).mustEqual("IS6@informatik.fh-schmalkalden.de")
      IS(4).mustEqual("IS5@informatik.fh-schmalkalden.de")
      IS(3).mustEqual("IS4@informatik.fh-schmalkalden.de")
      IS(2).mustEqual("IS3@informatik.fh-schmalkalden.de")
      IS(1).mustEqual("IS2@informatik.fh-schmalkalden.de")
      IS(0).mustEqual("IS1@informatik.fh-schmalkalden.de")

      MAI(0).mustEqual("MAI1@informatik.fh-schmalkalden.de")
      MAI(1).mustEqual("MAI2@informatik.fh-schmalkalden.de")
      MAI(2).mustEqual("MAI3@informatik.fh-schmalkalden.de")
      MAI(3).mustEqual("MAI4@informatik.fh-schmalkalden.de")
    }
  }
}
