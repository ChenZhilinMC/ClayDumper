package com.github.chenzhilinmc.claydumper.data.impl;

import java.util.HashMap;

public class ClayClass {
    private final String name;

    private final String obf;

    private final HashMap<String, ClayMethod> methods = new HashMap<>();

    private final HashMap<String, ClayField> fields = new HashMap<>();

    public ClayClass(final String name, final String obf) {
        this.name = name;
        this.obf = obf;
    }

    public String getName() {
        return name;
    }

    public String getObf() {
        return obf;
    }

    public void addField(final String name, final ClayField method) {
        this.fields.put(name, method);
    }

    public ClayField getField(final String name) {
        return this.fields.get(name);
    }

    public void addMethod(final String name, final ClayMethod method) {
        this.methods.put(name, method);
    }

    public ClayMethod getMethod(final String name) {
        return this.methods.get(name);
    }
}
