/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
module io.jooby.apt {
  exports io.jooby.apt;

  requires io.jooby;
  requires java.compiler;
  requires com.github.spotbugs.annotations;
  requires jakarta.inject;

  // SHADED: All content after this line will be removed at build time
  requires org.objectweb.asm;
  requires org.objectweb.asm.tree;
  requires org.objectweb.asm.tree.analysis;
  requires org.objectweb.asm.util;
}
