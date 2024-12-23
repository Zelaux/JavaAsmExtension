package test.value;

import asmlib.annotations.DebugAST;

//@ValueClass
@DebugAST(outfile = "test-print.java")
public class ComplexNumber {
    public final double im;
    public final double re;

    public ComplexNumber(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public ComplexNumber mul(ComplexNumber other) {
        return new ComplexNumber(
                re * other.re - im * other.im,
                re * other.im + im * other.re
        );
    }

    public ComplexNumber pow(double pow) {
        double len = Math.sqrt(magnitudeSquared()) * pow;
        double angle = Math.atan(im / re) * pow;

        return new ComplexNumber(
                Math.cos(angle) * len,
                Math.sin(angle) * len
        );
    }


    public double magnitudeSquared() {
        return re * re + im * im;
    }

    public ComplexNumber add(ComplexNumber other) {
        return new ComplexNumber(
                re + other.re,
                im + other.im
        );
    }
}
