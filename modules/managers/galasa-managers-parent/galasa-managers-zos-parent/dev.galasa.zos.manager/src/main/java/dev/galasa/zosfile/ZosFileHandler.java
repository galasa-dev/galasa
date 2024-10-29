/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * z/OS File 
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosFileHandler</code> annotation requests the z/OS Manager to provide a
 * handler instance to manage data sets and UNIX files on a z/OS image. 
 * A single z/OS File Handler instance can manage multiple z/OS data sets and UNIX files on multiple z/OS images.<br>
 * 
 * @galasa.examples 
 * <code>{@literal @}ZosFileHandler<br>
 * public IZosFileHandler zosFileHandler;<br></code>
 * 
 * @galasa.extra
 * The <code>IZosFileHandler</code> interface has three methods supplying file name and z/OS image:<br>
 * {@link IZosFileHandler#newDataset(String, dev.galasa.zos.IZosImage)}<br> 
 * {@link IZosFileHandler#newVSAMDataset(String, dev.galasa.zos.IZosImage)}<br>
 * {@link IZosFileHandler#newUNIXFile(String, dev.galasa.zos.IZosImage)}<br>
 * returning an object representing the type of file requested. This can be an existing file or can be created via a method on
 * the file object.<br><br>
 * See {@link ZosFileHandler}, {@link IZosFileHandler}, {@link IZosDataset}, {@link IZosVSAMDataset} and {@link IZosUNIXFile} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosFileField
@ValidAnnotatedFields({ IZosFileHandler.class })
public @interface ZosFileHandler {

}
