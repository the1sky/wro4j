/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.linter.AbstractLinter;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * Processor which analyze the js code and warns you about any problems. The processing result won't change no matter
 * if the processed script contains errors or not.
 *
 * @author Alex Objelean
 * @since 1.3.5
 * @created 1 Mar 2011
 */
@SupportedResourceType(ResourceType.JS)
public abstract class AbstractLinterProcessor
  implements ResourceProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractLinterProcessor.class);
  /**
   * Options to use to configure the linter.
   */
  private String[] options;


  public AbstractLinterProcessor setOptions(final String[] options) {
    this.options = options;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      newLinter().setOptions(options).validate(content);
    } catch (final LinterException e) {
      try {
        onLinterException(e, resource);
      } catch (final Exception ex) {
        throw new WroRuntimeException("", ex);
      }
    } catch (final WroRuntimeException e) {
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      // don't change the processed content no matter what happens.
      writer.write(content);
      reader.close();
      writer.close();
    }
  }

  /**
   * @return the linter to use for js code validation.
   */
  protected abstract AbstractLinter newLinter();

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer) throws IOException {
    process(null, reader, writer);
  }

  /**
   * Called when {@link LinterException} is thrown. Allows subclasses to re-throw this exception as a
   * {@link RuntimeException} or handle it differently. The default implementation simply logs the errors.
   *
   * @param e {@link LinterException} which has occurred.
   * @param resource the processed resource which caused the exception.
   */
  protected void onLinterException(final LinterException e, final Resource resource)
    throws Exception {
    LOG.error("The following resource: " + resource + " has " + e.getErrors().size() + " errors.", e);
  }
}