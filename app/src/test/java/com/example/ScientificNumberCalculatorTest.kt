package com.example

import com.example.engine.AngleUnit
import com.example.engine.BaseOperation
import com.example.engine.NumberBase
import com.example.engine.ScientificNumberCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScientificNumberCalculatorTest {

    @Test
    fun `scientific expression evaluates trig log power factorial and implicit multiplication`() {
        val trig = ScientificNumberCalculator.evaluateScientificExpression("sin(30) + 2^3", AngleUnit.DEG)
        assertTrue(trig.success)
        assertEquals("8.5", trig.formattedResult)

        val logFact = ScientificNumberCalculator.evaluateScientificExpression("log(100) + 5!", AngleUnit.DEG)
        assertTrue(logFact.success)
        assertEquals("122", logFact.formattedResult)

        val implicit = ScientificNumberCalculator.evaluateScientificExpression("2(3+4) + 50%", AngleUnit.DEG)
        assertTrue(implicit.success)
        assertEquals("14.5", implicit.formattedResult)

        val divZero = ScientificNumberCalculator.evaluateScientificExpression("5 / (2 - 2)", AngleUnit.DEG)
        assertFalse(divZero.success)
    }

    @Test
    fun `casio ms combinatorics hyperbolic gra fraction and memory registers work accurately`() {
        val ncr = ScientificNumberCalculator.evaluateScientificExpression("5C2 + 5P2", AngleUnit.DEG)
        assertTrue(ncr.success)
        assertEquals("30", ncr.formattedResult)

        val gra = ScientificNumberCalculator.evaluateScientificExpression("sin(100)", AngleUnit.GRA)
        assertTrue(gra.success)
        assertEquals("1", gra.formattedResult)

        val vars = ScientificNumberCalculator.evaluateScientificExpression(
            "2*X + A",
            AngleUnit.DEG,
            variables = mapOf("X" to 4.0, "A" to 3.0, "C" to 9.0)
        )
        assertTrue(vars.success)
        assertEquals("11", vars.formattedResult)

        val combWithVarC = ScientificNumberCalculator.evaluateScientificExpression(
            "6C2",
            AngleUnit.DEG,
            variables = mapOf("C" to 9.0)
        )
        assertTrue(combWithVarC.success)
        assertEquals("15", combWithVarC.formattedResult)

        assertEquals("2⌟3⌟4", ScientificNumberCalculator.toFractionString(2.75))
        assertEquals("1⌟2", ScientificNumberCalculator.toFractionString(0.5))
    }

    @Test
    fun `casio ms sd stats eqn complex and calculus solvers work accurately`() {
        val sd = ScientificNumberCalculator.computeSdStats(listOf(10.0, 20.0, 30.0))
        assertNotNull(sd)
        assertEquals(3, sd!!.n)
        assertEquals(20.0, sd.mean, 1e-6)
        assertEquals(10.0, sd.sampleStdDev, 1e-6)

        val quad = ScientificNumberCalculator.solveQuadratic(1.0, -5.0, 6.0)
        assertTrue(quad.any { it.contains("3") } && quad.any { it.contains("2") })

        val lin = ScientificNumberCalculator.solveLinear2x2(2.0, 1.0, 5.0, 1.0, -1.0, 1.0)
        assertTrue(lin.any { it.contains("x = 2") } && lin.any { it.contains("y = 1") })

        val cmplx = ScientificNumberCalculator.evaluateComplex(3.0, 4.0, 1.0, -2.0, "+", AngleUnit.DEG)
        assertNotNull(cmplx)
        assertEquals("4 + 2i", cmplx!!.rectangularText)

        val deriv = ScientificNumberCalculator.evaluateDerivative("X^2 + 2*X", 3.0)
        assertTrue(deriv.success)
        assertEquals(8.0, deriv.numericValue!!, 1e-4)

        val integ = ScientificNumberCalculator.evaluateIntegral("X^2", 0.0, 3.0)
        assertTrue(integ.success)
        assertEquals(9.0, integ.numericValue!!, 1e-4)
    }

    @Test
    fun `number base converter handles integer and fractional conversions accurately`() {
        val decFrac = ScientificNumberCalculator.convertNumberBase("25.625", NumberBase.DEC, NumberBase.BIN)
        assertTrue(decFrac.valid)
        assertEquals("11001.101", decFrac.binary)
        assertEquals("31.5", decFrac.octal)
        assertEquals("25.625", decFrac.decimal)
        assertEquals("19.A", decFrac.hex)

        val hexToBin = ScientificNumberCalculator.convertNumberBase("3F.A", NumberBase.HEX, NumberBase.BIN)
        assertTrue(hexToBin.valid)
        assertEquals("111111.101", hexToBin.binary)
        assertEquals("77.5", hexToBin.octal)
        assertEquals("63.625", hexToBin.decimal)
    }

    @Test
    fun `base arithmetic and bitwise operations compute all bases accurately`() {
        val binAdd = ScientificNumberCalculator.evaluateBaseArithmetic("1011", "1101", NumberBase.BIN, BaseOperation.ADD)
        assertTrue(binAdd.valid)
        assertEquals("11000", binAdd.conversion?.binary)
        assertEquals("24", binAdd.conversion?.decimal)

        val hexAdd = ScientificNumberCalculator.evaluateBaseArithmetic("2A", "1F", NumberBase.HEX, BaseOperation.ADD)
        assertTrue(hexAdd.valid)
        assertEquals("49", hexAdd.conversion?.hex)
        assertEquals("73", hexAdd.conversion?.decimal)

        val xorRes = ScientificNumberCalculator.evaluateBaseArithmetic("1101", "1011", NumberBase.BIN, BaseOperation.XOR)
        assertTrue(xorRes.valid)
        assertEquals("110", xorRes.conversion?.binary)
    }

    @Test
    fun `twos complement handles positive negative and subtraction`() {
        val subPos = ScientificNumberCalculator.computeTwosComplement(25, 14, isSubtract = true, bitWidth = 8)
        assertTrue(subPos.valid)
        assertEquals(11, subPos.decimalResult)
        assertEquals("00001011", subPos.binaryResult)

        val subNeg = ScientificNumberCalculator.computeTwosComplement(14, 25, isSubtract = true, bitWidth = 8)
        assertTrue(subNeg.valid)
        assertEquals(-11, subNeg.decimalResult)
        assertEquals("11110101", subNeg.binaryResult)
    }
}
