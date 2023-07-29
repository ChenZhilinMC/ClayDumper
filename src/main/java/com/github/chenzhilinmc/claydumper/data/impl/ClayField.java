package com.github.chenzhilinmc.claydumper.data.impl;

public class ClayField {
    private final String name;
    private final String obf;
    private final String desc;

    public ClayField(final String name, final String obf, final String desc) {
        this.name = name;
        this.obf = obf;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getObf() {
        return obf;
    }

    public String getDesc() {
        return desc;
    }
}
