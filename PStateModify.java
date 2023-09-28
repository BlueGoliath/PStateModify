import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class PStateModify
{
    public static void main(String[] args) throws Throwable
    {
        int gpu = 0;
        int state = 16;
        String library = "libnvidia-api.so";
        
        for(int i = 0; i < args.length; i++)
        {
            if("-i".equals(args[i]))
                gpu = Integer.parseInt(args[i + 1]);

            if("-s".equals(args[i]))
                state = Integer.parseInt(args[i + 1]);

            if("-l".equals(args[i]))
                library = args[i + 1];
        }
        
        SymbolLookup lookup = SymbolLookup.libraryLookup(
                library, Arena.ofShared());
        
        MemorySegment interfaceSegment = lookup.find(
                "nvapi_QueryInterface").get();
        
        MethodHandle interfaceHandle = Linker.nativeLinker().downcallHandle(
                interfaceSegment,
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS, 
                        ValueLayout.JAVA_INT));
        
        MemorySegment initSegment = (MemorySegment)interfaceHandle.invoke(
                0x0150e828);
        
        MemorySegment unloadSegment = (MemorySegment)interfaceHandle.invoke(
                0xd22bdd7e);
        
        MemorySegment enumSegment = (MemorySegment)interfaceHandle.invoke(
                0xe5ac921f);
        
        MemorySegment stateSegment = (MemorySegment)interfaceHandle.invoke(
                0x025BFB10);

        if(initSegment.equals(MemorySegment.NULL))
        {
            System.out.println("Failed to get init symbol!");
            System.exit(1);
        }
        
        if(unloadSegment.equals(MemorySegment.NULL))
        {
            System.out.println("Failed to get unload symbol!");
            System.exit(2);
        }
        
        if(enumSegment.equals(MemorySegment.NULL))
        {
            System.out.println("Failed to get enum symbol!");
            System.exit(3);
        }
        
        if(stateSegment.equals(MemorySegment.NULL))
        {
            System.out.println("Failed to get state symbol!");
            System.exit(4);
        }
        
        MethodHandle initUnloadHandle = Linker.nativeLinker().downcallHandle(
        FunctionDescriptor.of(ValueLayout.JAVA_INT));
        
        MethodHandle initHandle = initUnloadHandle.bindTo(initSegment);
        MethodHandle unloadHandle = initUnloadHandle.bindTo(unloadSegment);
        
        int initResult = (int)initHandle.invoke();
        
        if(initResult != 0)
        {
            System.out.println("Failed to init!");
            System.exit(5);
        }
        
        MemorySegment gpuArray = Arena.ofAuto().allocate(
        MemoryLayout.sequenceLayout(
                64, 
                ValueLayout.ADDRESS));
        
        MemorySegment gpuCount = Arena.ofAuto().allocate(4);
        
        MethodHandle enumHandle = Linker.nativeLinker().downcallHandle(
                enumSegment,
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS));
        
        int enumResult = (int)enumHandle.invoke(gpuArray, gpuCount);
       
        if(enumResult != 0)
        {
            System.out.println("Failed to enum GPUs!");
            System.exit(6);
        }
        
        MemorySegment gpuSegment = gpuArray.asSlice(gpu);
        
        MethodHandle stateHandle = Linker.nativeLinker().downcallHandle(
                stateSegment,
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT));
        
        int stateResult = (int)stateHandle.invoke(
                gpuSegment.get(
                        ValueLayout.ADDRESS,
                        0),
                state,
                2);
        
        if(stateResult != 0)
        {
            System.out.println("Failed to set performance state! ");
            System.exit(7);
        }
        else
            System.out.println("Performance state has been set successfully.");
        
        unloadHandle.invoke();
    }
}
