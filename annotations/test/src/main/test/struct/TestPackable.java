package test.struct;

import asmlib.annotations.DebugAST;
//import asmlib.annotations.Packable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.core.PrintAST;

//@Packable
@AllArgsConstructor
@NoArgsConstructor
@DebugAST(outfile = "tmp.java")
public class TestPackable {
    @With
    public byte x, y;


//    @Packable.Unpack
    public static native TestPackable unpack(short type);

//    @Packable.Pack
    public native short pack();
}
