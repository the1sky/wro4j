/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.yui.YUIErrorReporter;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;


/**
 * YUICssCompressorProcessor - an adapter for YUI js compression utility for processing js resources.
 *
 * @author Alex Objelean
 * @created Created on Dec 4, 2008
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class YUIJsCompressorProcessor
  implements ResourcePostProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(YUIJsCompressorProcessor.class);
  // options of YUI compressor
  private final int linebreakpos = -1;
  boolean munge = true;
  boolean preserveAllSemiColons = true;
  boolean disableOptimizations = false;
  boolean verbose = true;


  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    final StringBuffer sb = new StringBuffer();
    try {
      final JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new YUIErrorReporter(sb));
      /*
       * Params for this call: Writer: the StringWriter. int linebreak: always -1... boolean munge: obfuscated or not.
       * boolean verbose: logging level boolean preserveAllSemiColons: leave to false. boolean disableOptimizations:
       * leave to true. *
       */
      compressor.compress(writer, -1, true, true, false, false);
    } finally {
      System.out.println(sb);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void process1(final Reader reader, final Writer writer)
    throws IOException {
    try {
      final JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new ErrorReporter() {
        public void warning(final String message, final String sourceName, final int line, final String lineSource,
          final int lineOffset) {
          if (line < 0) {
            LOG.warn("\n[WARNING] " + message);
          } else {
            LOG.warn("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
          }
        }


        public void error(final String message, final String sourceName, final int line, final String lineSource,
          final int lineOffset) {
          if (line < 0) {
            LOG.error("\n[ERROR] " + message);
          } else {
            LOG.error("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
          }
        }


        public EvaluatorException runtimeError(final String message, final String sourceName, final int line,
          final String lineSource, final int lineOffset) {
          error(message, sourceName, line, lineSource, lineOffset);
          return new EvaluatorException(message);
        }
      });
      compressor.compress(writer, linebreakpos, munge, verbose, preserveAllSemiColons, disableOptimizations);
    } catch (final RuntimeException e) {
      throw new WroRuntimeException("Problem while applying YUI compressor", e);
    } finally {
      reader.close();
      writer.close();
    }
  }
}
