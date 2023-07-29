package com.github.chenzhilinmc.claydumper.data;

import com.github.chenzhilinmc.claydumper.data.impl.ClayField;
import com.google.gson.Gson;
import com.github.chenzhilinmc.claydumper.data.enums.TargetType;
import com.github.chenzhilinmc.claydumper.data.impl.ClayClass;
import com.github.chenzhilinmc.claydumper.data.impl.ClayMethod;

import java.util.HashMap;

public class ClayCore {
    private String compile_time;
    private HashMap<String, ClayClass> mappings;

    public HashMap<String, ClayClass> getMappings() {
        return mappings;
    }

    public void setMappings(final HashMap<String, ClayClass> mappings) {
        this.mappings = mappings;
    }

    public String getCompileTime() {
        return compile_time;
    }

    public void setCompileTime(final String compile_time) {
        this.compile_time = compile_time;
    }

    public final String toJson() {
        return new Gson().toJson(this);
    }

    public void putResult(final String className, final String name, final String obf, final String desc, final TargetType type) {
        final var s = mappings.get(className);
        if (s != null) {
            switch (type) {
                case FIELD -> {
                    s.addField(name, new ClayField(name, obf, desc));
                }
                case METHOD -> {
                    s.addMethod(name, new ClayMethod(name, obf, desc));
                }
            }
        } else {
            throw new RuntimeException("unknown clay class");
        }
    }

    public void putClassResult(final String name, final String obf) {
        mappings.put(name, new ClayClass(name, obf));
    }
}
