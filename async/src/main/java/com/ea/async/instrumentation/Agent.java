package com.ea.async.instrumentation;

import java.lang.instrument.Instrumentation;

/**
 * Class called when a java agent is attached to the JVM in runtime.
 */
public class Agent {

    /*
     * From https://docs.oracle.com/javase/8/docs/api/index.html?java/lang/instrument/Instrumentation.html
     * 
     * Agent-Class
     * 
     * If an implementation supports a mechanism to start agents sometime after the VM has started, 
     * then this attribute specifies the agent class. That is, the class containing the agentmain method. 
     * This attribute is required; if it is not present, the agent will not be started.
     * Note: this is a class name, not a file name or path.
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        Transformer transformer = new Transformer();
        inst.addTransformer(transformer, true);

        // Loop through all loaded classes and retransform the ones that need instrumentation
        f1:
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (inst.isModifiableClass(clazz)
                    && !clazz.getName().startsWith("java.")
                    && !clazz.getName().startsWith("javax.")
                    && !clazz.getName().startsWith("sun.")) {
                try {
                    // Avoid retransformation if the class is already instrumented
                    if (transformer.needsInstrumentation(clazz) && !alreadyInstrumented(clazz)) {
                        inst.retransformClasses(clazz);
                        markAsInstrumented(clazz);
                    }
                } catch (Exception | Error e) {
                    e.printStackTrace();
                }
            }
        }

        // Set a system property to indicate that EA async is running
        System.setProperty(Transformer.EA_ASYNC_RUNNING, "true");
    }

    // Helper method to check if a class has already been instrumented by this agent
    private static boolean alreadyInstrumented(Class<?> clazz) {
        return System.getProperty("instrumented." + clazz.getName()) != null;
    }

    // Helper method to mark a class as instrumented
    private static void markAsInstrumented(Class<?> clazz) {
        System.setProperty("instrumented." + clazz.getName(), "true");
    }
}
