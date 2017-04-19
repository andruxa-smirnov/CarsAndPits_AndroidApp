package org.ubjson;

import java.io.FileOutputStream;
import java.io.IOException;

import org.ubjson.io.UBJInputStream;
import org.ubjson.io.UBJOutputStream;

public class MediaContentMarshaller {
	/*
	 * Used to write out the test UBJ file handled by this class. Typically only
	 * needed by author to write a new test UBJ file if the file format changes
	 * due to an update to the POJO or marshaling logic.
	 */
	public static void main(String[] args) throws IOException {
		serialize(new MediaContent(), new UBJOutputStream(new FileOutputStream(
				"MediaContent.ubj")));
	}

	public static void serialize(MediaContent mc, UBJOutputStream out)
			throws IOException {
		// root obj
		out.writeObjectHeader(2);

		// media obj
		out.writeString("Media");
		out.writeObjectHeader(11);

		// media: contents
		out.writeString("uri");
		out.writeString(mc.media.uri);

		out.writeString("title");
		out.writeString(mc.media.title);

		out.writeString("width");
		out.writeInt32(mc.media.width);

		out.writeString("height");
		out.writeInt32(mc.media.height);

		out.writeString("format");
		out.writeString(mc.media.format);

		out.writeString("duration");
		out.writeInt32(mc.media.duration);

		out.writeString("size");
		out.writeInt32(mc.media.size);

		out.writeString("bitrate");
		out.writeInt32(mc.media.bitrate);

		out.writeString("persons");
		out.writeArrayHeader(2);
		out.writeString(mc.media.persons[0]);
		out.writeString(mc.media.persons[1]);

		out.writeString("player");
		out.writeString(mc.media.player);

		out.writeString("copyright");
		out.writeNull();

		// images array
		out.writeString("Images");
		out.writeArrayHeader(2);

		// array[0]
		out.writeObjectHeader(5);
		out.writeString("uri");
		out.writeString(mc.images[0].uri);
		out.writeString("title");
		out.writeString(mc.images[0].title);
		out.writeString("width");
		out.writeInt32(mc.images[0].width);
		out.writeString("height");
		out.writeInt32(mc.images[0].height);
		out.writeString("size");
		out.writeString(mc.images[0].size);

		// array[1]
		out.writeObjectHeader(5);
		out.writeString("uri");
		out.writeString(mc.images[1].uri);
		out.writeString("title");
		out.writeString(mc.images[1].title);
		out.writeString("width");
		out.writeInt32(mc.images[1].width);
		out.writeString("height");
		out.writeInt32(mc.images[1].height);
		out.writeString("size");
		out.writeString(mc.images[1].size);
	}

	public static MediaContent deserialize(UBJInputStream in)
			throws IOException {
		MediaContent mc = new MediaContent();

		// root obj
		in.readObjectLength();

		// media obj
		in.readStringAsChars();
		in.readObjectLength();

		// media:contents
		in.readStringAsChars();
		mc.media.uri = in.readString();

		in.readStringAsChars();
		mc.media.title = in.readString();

		in.readStringAsChars();
		mc.media.width = in.readInt32();

		in.readStringAsChars();
		mc.media.height = in.readInt32();

		in.readStringAsChars();
		mc.media.format = in.readString();

		in.readStringAsChars();
		mc.media.duration = in.readInt32();

		in.readStringAsChars();
		mc.media.size = in.readInt32();

		in.readStringAsChars();
		mc.media.bitrate = in.readInt32();

		in.readStringAsChars();
		in.readArrayLength();
		mc.media.persons = new String[2];
		mc.media.persons[0] = in.readString();
		mc.media.persons[1] = in.readString();

		in.readStringAsChars();
		mc.media.player = in.readString();

		in.readStringAsChars();
		mc.media.copyright = (in.read() == 'Z' ? null : "");

		if (mc.media.copyright != null)
			throw new RuntimeException(
					"Did not read NULL value for copyright field.");

		// images array
		in.readStringAsChars();
		in.readArrayLength();
		mc.images = new MediaContent.Image[2];
		mc.images[0] = new MediaContent.Image(null, null, 0, 0, null);
		mc.images[1] = new MediaContent.Image(null, null, 0, 0, null);

		// array[0]
		in.readObjectLength();
		in.readStringAsChars();
		mc.images[0].uri = in.readString();
		in.readStringAsChars();
		mc.images[0].title = in.readString();
		in.readStringAsChars();
		mc.images[0].width = in.readInt32();
		in.readStringAsChars();
		mc.images[0].height = in.readInt32();
		in.readStringAsChars();
		mc.images[0].size = in.readString();

		// array[1]
		in.readObjectLength();
		in.readStringAsChars();
		mc.images[1].uri = in.readString();
		in.readStringAsChars();
		mc.images[1].title = in.readString();
		in.readStringAsChars();
		mc.images[1].width = in.readInt32();
		in.readStringAsChars();
		mc.images[1].height = in.readInt32();
		in.readStringAsChars();
		mc.images[1].size = in.readString();

		return mc;
	}
}