package org.ugp.serialx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

/**
 * Interface with common methods for serializing and deserializing/loading contents from various types of media ({@link File}, {@link InputStream}, {@link Appendable}, {@link Reader} etc...).<br>
 * Intended/recommended use is to implement this together with some sort of {@link Collection} or {@link Map} and/or something that can be treated as such.
 * 
 * @param <T> Type of the object that is serialized and deserialized, you likely want this to be the class (or patent class) of an implementor of this interface.
 * 
 * @author PETO
 *
 * @since 4.0.0 (Separated from {@link Serializer} in 4.0.0)
 */
public interface MultimediaSerializer<T>
{
	/**
	 * @param f | File to write in. This must be a text file.
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @throws IOException if file can't be opened or serialization fails!
	 * 
	 * @since 1.1.5		
	 */
	default void serializeTo(File f, Object... args) throws IOException
	{
		serializeTo(false, f, args);
	}
	
	/**
	 * @param append | When true, the new objects will be appended to files content (same objects will be also appended if there are some)! 
	 * @param f | File to write in. This must be a text file.
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @throws IOException if file can't be opened or serialization fails!
	 * 
	 * @since 1.1.5
	 * 
	 * @see MultimediaSerializer#serializeTo(Appendable, Object...)
	 */
	default void serializeTo(boolean append, File f, Object... args) throws IOException
	{
		//double t0 = System.nanoTime();
		try (Writer writer = new BufferedWriter(new FileWriter(f, append)))
		{
			serializeTo(writer, args);
		}

//		String serialized = stringify(args);
//
//		RandomAccessFile fileOutputStream = new RandomAccessFile(f, "rw");
//		FileChannel channel = fileOutputStream.getChannel();
//
////		channel.write(ByteBuffer.wrap(serialized.getBytes()));
//		
//		ByteBuffer buff = channel.map(FileChannel.MapMode.READ_WRITE, append ? channel.size() : 0, serialized.length());
//		buff.put(serialized.getBytes());
//
//		channel.force(true);
//		channel.close();
//		fileOutputStream.close();
	}
	
	/**
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return String with the respective contents serialized in specific format.
	 * 
	 * @throws RuntimeException IOException, but it should not occur in this case...
	 * 
	 * @since 1.0.0
	 * 
	 * @see MultimediaSerializer#serializeTo(Appendable, Object...)
	 */
	default String stringify(Object... args)
	{
		try
		{
			return serializeTo(new StringBuilder(), args).toString();
		} 
		catch (IOException e) 
		{
			throw new RuntimeException(e); // Should not occur...
		}
	}
	
	/**
	 * @param outputStream | Source {@link OutputStream} to serialize the respective contents into!
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return Source {@link OutputStream} with respective contents serialized in specific format (may or may not be flushed).
	 * 
	 * @throws IOException When appending the {@link OutputStream} throws.... 
	 * 
	 * @since 1.3.2
	 * 
	 * @see MultimediaSerializer#serializeTo(Appendable, Object...)
	 */
	default OutputStream serializeTo(OutputStream outputStream, Object... args) throws IOException
	{
		serializeTo(new OutputStreamWriter(outputStream), args);
		return outputStream;
	}
	
	/**
	 * @param source | Source {@link Appendable} to serialize respective contents into!
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return Source {@link Appendable} with respective contents serialized in specific format (depends on the implementation).
	 * 
	 * @throws IOException depends on the implementation (likely when the provided source throws). 
	 * 
	 * @since 1.3.2
	 */
	<A extends Appendable> A serializeTo(A source, Object... args) throws IOException;
	
	/**
	 * @param source | Source {@link Appendable} to serialize respective contents into!
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return Serialized contents as the inner/nested sub-scope of this {@link MultimediaSerializer}! Wrapped inside of corresponding wrappingBrackets (default { and })! This is more usecase specific, mainly useful with treelike structures.
	 * 
	 * @since 1.3.5
	 */
	default <A extends Appendable> A serializeAsSubscope(A source, Object... args) throws IOException
	{
		return serializeAsSubscope(source, new char[] {'{', '}'}, args);
	}

	/**
	 * @param source | Source {@link Appendable} to serialize respective contents into!
	 * @param wrappingBrackets | Array of 2 characters to wrap content inside (opening and closing one).
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return Serialized contents as the inner/nested sub-scope of this {@link MultimediaSerializer}! Wrapped inside of corresponding wrappingBrackets (default { and })! This is more usecase specific, mainly useful with treelike structures.
	 * 
	 * @since 1.3.5
	 */
	<A extends Appendable> A serializeAsSubscope(A source, final char[] wrappingBrackets, Object... args) throws IOException;

	/**
	 * @param file | Text file with serialized contents in specific format to load.
	 * @param parserArgs | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (probably used for formating of incoming information)!
	 * 
	 * @return Unserialized contents represented by object <code>T</code> (may or may not be "this")!
	 * 
	 * @throws IOException if file does not exist or cannot be read!
	 * 
	 * @since 1.0.0
	 * 
	 * @see MultimediaSerializer#loadFrom(Reader, Object...)
	 */
	default <S extends T> S loadFrom(File file, Object... parserArgs) throws IOException
	{
		return loadFrom(new FileReader(file), parserArgs);
	}
	
	/**
	 * @param str | {@link CharSequence} with serialized contents in specific format to load.
	 * @param parserArgs | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (probably used for formating of incoming information)!
	 * 
	 * @return Unserialized contents represented by object <code>T</code> (may or may not be "this")!
	 * 
	 * @throws IOException When reading the char sequence fails...
	 * 
	 * @since 1.2.5
	 * 
	 * @see MultimediaSerializer#loadFrom(Reader, Object...)
	 */
	default <S extends T> S loadFrom(CharSequence str, Object... parserArgs) throws IOException
	{
		return loadFrom(new StringReader(str.toString()), parserArgs);
	}
	
	/**
	 * @param stream | Any {@link InputStream} with serialized contents in specific format to load.
	 * @param parserArgs | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (probably used for formating of incoming information)!
	 * 
	 * @return Unserialized contents represented by object <code>T</code> (may or may not be "this")!
	 * 
	 * @throws IOException When reading the input stream fails...
	 * 
	 * @since 1.3.2
	 * 
	 * @see MultimediaSerializer#loadFrom(Reader, Object...)
	 */
	default <S extends T> S loadFrom(InputStream stream, Object... parserArgs) throws IOException
	{
		return loadFrom(new InputStreamReader(stream), parserArgs);
	}
	
	/**
	 * @param reader | Reader to read the with serialized contents from!
	 * @param parserArgs | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (probably used for formating of incoming information)!
	 * 
	 * @return Unserialized contents represented by object <code>T</code> (may or may not be "this". If <code>T</code> is instanceof this Class, you most likely want to return "this")!
	 * 
	 * @throws IOException When reading the reader fails...
	 * 
	 * @since 1.3.2
	 */
	<S extends T> S loadFrom(Reader reader, Object... parserArgs) throws IOException;
}
