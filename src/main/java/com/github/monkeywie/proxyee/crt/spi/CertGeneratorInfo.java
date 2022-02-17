package com.github.monkeywie.proxyee.crt.spi;

import java.lang.annotation.*;

/**
 * <div class="zh">
 *   证书生成器注解.
 *   <p>用于标注声明生成器的具体不可变信息.</p>
 * </div>
 * <div class="en">
 *   Certificate generator annotations.
 *   <p>Concrete immutable information used to annotate the declaration generator.</p>
 * </div>
 * @author LamGC
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CertGeneratorInfo {

    /**
     * <div class="zh">
     *   生成器名称, 该名称要求唯一, 不可重复.
     *   <p>当遇到名称相同的不同生成器实例时, 将选择第一个加载的实现.</p>
     * </div>
     * <div class="en">
     *   Generator name, the name must be unique and cannot be repeated.
     *   <p>When a different generator instance with the same name is encountered, the first loaded implementation will be chosen.</p>
     * </div>
     * @return <div class="zh">返回生成器唯一名称.</div><div class="en">Returns the generator unique name.</div>
     */
    String name();

}
