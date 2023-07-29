package com.github.chenzhilinmc.claydumper;

import com.github.chenzhilinmc.claydumper.data.ClayCore;
import com.github.chenzhilinmc.claydumper.data.enums.Find;
import com.github.chenzhilinmc.claydumper.data.enums.TargetType;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarFile;

public class Dumper {
    private final HashSet<ClassNode> clayClasses = new HashSet<>();
    private final File claycore_file;

    public Dumper(final File claycore_file) {
        this.claycore_file = claycore_file;
    }

    public final ClayCore dump() {
        final ClayCore core = new ClayCore();
        try (final var jar_file = new JarFile(claycore_file);) {
            core.setCompileTime(jar_file.getManifest().getMainAttributes().getValue("Implementation-Timestamp"));
            for (final var iterator = jar_file.entries(); iterator.hasMoreElements(); ) {
                final var entry = iterator.nextElement();
                try (final var in = jar_file.getInputStream(entry)) {
                    if ((entry.getName().startsWith("clay/") || entry.getName().startsWith("com/trychen/clay/core/mixin")) && entry.getName().endsWith(".class")) {
                        final var reader = new ClassReader(in);
                        final var classNode = new ClassNode();
                        reader.accept(classNode, 0);
                        clayClasses.add(classNode);
                    }
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        for (final var current_class : clayClasses) {
            if ("com/trychen/clay/core/mixin/MixinItemInHandRenderer".equals(current_class.name)) {
                for (final var method : current_class.methods) {
                    if (method.desc.endsWith("F)F")) {
                        for (final AbstractInsnNode instruction : method.instructions) {
                            if (instruction instanceof MethodInsnNode node && node.owner.startsWith("clay/")) {
                                core.putClassResult("ClayShootState", node.owner);
                            };
                        }
                    }
                }
            }
            final var current_fields = current_class.fields;
            for (final var current_field : current_fields) {
                if ("Lnet/minecraftforge/network/simple/SimpleChannel;".equals(current_field.desc)) {
                    core.putClassResult("NetworkHandler", current_class.name);
                    core.putResult("NetworkHandler", "channel", current_field.name, current_field.desc, TargetType.FIELD);
                    for (final var method : current_class.methods) {
                        if ("(Ljava/lang/String;[Ljava/lang/Object;)V".equals(method.desc)) {
                            core.putResult("NetworkHandler", "sendDirectly", method.name, method.desc, TargetType.METHOD);
                            final var sendPacketWithXorMethod = current_class.methods.get(current_class.methods.indexOf(method) + 1);
                            core.putResult("NetworkHandler", "send", sendPacketWithXorMethod.name, sendPacketWithXorMethod.desc, TargetType.METHOD);
                        }
                    }
                }

            }
            final var current_methods = current_class.methods;
            for (final var method : current_methods) {
                if (core.getMappings().get("ClayShootState") != null) {
                    final var clayShootState = core.getMappings().get("ClayShootState");
                    if (clayShootState.getObf().equals(current_class.name) && clayShootState.getMethod("getCurrentWeapon") == null) {
                        for (final var methodNode : current_methods) {
                            if (methodNode.desc.startsWith("()Lclay/")) {
                                core.putResult("ClayShootState", "getCurrentWeapon", methodNode.name, methodNode.desc, TargetType.METHOD);
                            }
                        }
                    }
                }
                if ("<init>".equals(method.name)) {
                    if (method.desc.endsWith("Ljava/lang/String;ILjava/util/UUID;Ljava/util/UUID;DDDDFZDI)V")) {
                        core.putClassResult("BulletHitEntityResult", current_class.name);
                    };
                }
                switch (findAnnotations(method.visibleAnnotations, "SSV2.Upload", "ClayShoot.Friends", "ClayShoot.ClientShoot", "Anti.RequestClientInfo", "COD.Container.Data.UnlockedBackpack", "CodeRunner.Execute")) {
                    case "SSV2.Upload" -> {
                        core.putClassResult("ScreenShotUpload", current_class.name);
                        core.putResult("ScreenShotUpload", "upload", method.name, method.desc, TargetType.METHOD);
                    }
                    case "ClayShoot.Friends" -> {
                        core.putClassResult("ClayShootFriends", current_class.name);
                        core.putResult("ClayShootFriends", "setFriends", method.name, method.desc, TargetType.METHOD);
                        //setFriends方法向上三个就是获取队友Set
                        final var MAGIC = 3;
                        final var getFriendsSetMethod = current_methods.get(current_methods.indexOf(method) - MAGIC);
                        core.putResult("ClayShootFriends", "getFriendsSet", getFriendsSetMethod.name, getFriendsSetMethod.desc, TargetType.METHOD);
                    }
                    case "ClayShoot.ClientShoot" -> {
                        core.putClassResult("ClayShootLaunch", current_class.name);
                        //shoot方法向上一个是tryShoot
                        final var MAGIC = 1;
                        final var tryShootMethod = current_methods.get(current_methods.indexOf(method) - MAGIC);
                        core.putResult("ClayShootLaunch", "tryShoot", tryShootMethod.name, tryShootMethod.desc, TargetType.METHOD);
                    }
                    case "Anti.RequestClientInfo" -> {
                        core.putClassResult("AntiClientInfo", current_class.name);
                        final var sendMethod = findMethodByDesc(current_methods, "(Ljava/util/Map;)V", Find.EQUALS);
                        core.putResult("AntiClientInfo", "send", sendMethod.name, sendMethod.desc, TargetType.METHOD);
                        final var buildInfoMethod = findMethodByDesc(current_methods, "()Ljava/util/Map;", Find.EQUALS);
                        core.putResult("AntiClientInfo", "buildClientInfo", buildInfoMethod.name, buildInfoMethod.desc, TargetType.METHOD);
                    }
                    case "COD.Container.Data.UnlockedBackpack" -> {
                        core.putClassResult("FallenGlobal", current_class.name);
                        final var selectBackpackInGameMethod = current_methods.get(current_methods.indexOf(method) + 1);
                        core.putResult("FallenGlobal", "selectBackpackInGame", selectBackpackInGameMethod.name, selectBackpackInGameMethod.desc, TargetType.METHOD);
                    }
                    case "CodeRunner.Execute" -> {
                        core.putClassResult("CodeRunner", current_class.name);
                        core.putResult("CodeRunner", "execute", method.name, method.desc, TargetType.METHOD);
                    }
                }
            }
        }
        for (final ClassNode clayClass : clayClasses) {
            for (final var method : clayClass.methods) {
                if (String.format("(L%s;)V",core.getMappings().get("BulletHitEntityResult").getObf()).equals(method.desc)) {
                    core.putClassResult("ClayShootServer", clayClass.name);
                    core.putResult("ClayShootServer", "hitEntity", method.name, method.desc, TargetType.METHOD);
                }
            }
        }
        return core;
    }

    private MethodNode findMethodByDesc(final List<MethodNode> list, final String desc, final Find mode) {
        var temp = new MethodNode();
        for (final MethodNode methodNode : list) {
            switch (mode) {
                case EQUALS -> {
                    if (desc.equals(methodNode.desc)) {
                        temp = methodNode;
                    }
                }
                case ENDWITH -> {
                    if (methodNode.desc.endsWith(desc)) {
                        temp = methodNode;
                    }
                }
            }
        }
        return temp;
    }

    private String findAnnotations(final List<AnnotationNode> annotationNodeList, String... target) {
        if (annotationNodeList != null) {
            for (final var next : annotationNodeList) {
                if (next.values != null) {
                    for (final var value : next.values) {
                        for (final var s : target) {
                            if (s.equals(value)) return s;
                        }
                    }
                }
            }
        }
        return "die";
    }

    public static void main(String[] args) {
        final ClayCore dump = new Dumper(new File(".\\粘土云核心2023-05-13-23-14-24编译.jar")).dump();
        System.out.println(dump.toJson());
    }
}
