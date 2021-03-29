package com.blemesh.sdk.session;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;
import com.google.common.base.Objects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.blemesh.sdk.mesh_graph.LocalPeer;
import timber.log.Timber;

/**
 * Represents a Session segment suitable for transport via a {@link com.blemesh.sdk.transport.Transport}
 *
 * Requirements of Child Classes:
 *
 * Constructors must call {@link #serializeAndCacheHeaders()}.
 *
 * Constructors must set {@link #status} to {@link Status#HEADER_ONLY} if appropriate
 */
public abstract class SessionMessage {

    public static enum Status { HEADER_ONLY, COMPLETE }

    /** SessionMessage version. Must be representable by {@link #HEADER_VERSION_BYTES} bytes */
    public static final int CURRENT_HEADER_VERSION = 1;

    /** Leading byte specifies header format version */
    public static final int HEADER_VERSION_BYTES   = 1;

    /** Next bytes specify header size in bytes as uint16. Max header size: 65.535 kB */
    public static final int HEADER_LENGTH_BYTES    = 2;

    /** Required header map keys */
    public static final String HEADER_TYPE         = "type";
    public static final String HEADER_BODY_LENGTH  = "body-length";
    public static final String HEADER_ID           = "id";
    public static final String HEADER_ALIAS  = "alias";
    //public static final String HEADER_MAC_ADDRESS  = "mac_address";

    protected          int                     version;
    protected @NonNull String                  type;
    protected          int                     bodyLengthBytes;
    protected @NonNull String                  id;
    protected @NonNull Status                  status;
    protected          Map<String, Object>     headers;
    protected @NonNull String                   alias;
    //protected @NonNull String                  mac_address;
    private            byte[]                  serializedHeaders;

    /**
     * Construct a SessionMessage with a given id.
     * This constructor should be used for deserialization of
     * incoming SessionMessages.
     */
    public SessionMessage(@NonNull String src_alias, @NonNull String id) {
        this.id         = id;
        this.alias = src_alias;
        type            = getClass().getSimpleName();
        bodyLengthBytes = 0;
        version         = CURRENT_HEADER_VERSION;
        status          = Status.COMPLETE;

        // Child classes must call serializeAndCacheHeaders()
        // in their constructors

        // Child classes must set status to Status.HEADER_ONLY if they
        // are constructed without a body but expect one to become available
    }

    /**
     * Construct a new SessionMessage with a unique identifier.
     * This constructor should be used for creating new outgoing SessionMessages
     */
    public SessionMessage() {
        //TODO: LocalPeer.getLocalMacAddress()
        this(LocalPeer.getLocalAlias(),UUID.randomUUID().toString().substring(28));
    }


    /**
     * @return a HashMap representation of the message headers.
     * This method will be called once, and so it should not
     * rely on state that cannot be represented on construction.
     */
    protected HashMap<String, Object> populateHeaders() {
        HashMap<String, Object> headerMap = new HashMap<>();
        headerMap.put(HEADER_TYPE,        type);
        headerMap.put(HEADER_BODY_LENGTH, bodyLengthBytes);
        headerMap.put(HEADER_ID,          id);
        headerMap.put(HEADER_ALIAS, alias);

        return headerMap;
    }

    public @NonNull String getID(){return id;}

    public @NonNull String getAlias(){return alias;}

    public @NonNull String getType() {
        return type;
    }

    /**
     * @return the length of the serialized headers
     */
    public int getHeaderLengthBytes() {
        return serializedHeaders.length;
    }

    public @NonNull Map<String, Object> getHeaders() {
        return headers;
    }

    /**
     * @return the length of the blob body in bytes
     */
    public int getBodyLengthBytes() {
        return bodyLengthBytes;
    }

    public abstract @Nullable byte[] getBodyAtOffset(int offset, int length);

    /**
     * Serialize this SessionMessage for transport. Note that when the returned byte[]
     * has length less than given length or is null (data ended precisely on the last call),
     * serialization is complete.
     *
     * The general format of the serialized bytestream:
     *
     * byte idx | description
     * ---------|------------
     * [0]      | SessionMessage version
     * [1-2]    | Header length
     * [3-X]    | Header JSON. 'X' is value specified by Header length
     * [X-Y]    | Body. 'Y' is value specified in 'body-length' entry of Header JSON.
     *
     * @param length should never be less than {@link #HEADER_LENGTH_BYTES} + {@link #HEADER_VERSION_BYTES}
     *
     */
    public @Nullable byte[] serialize(int offset, int length) {
        if (offset < 0)
            throw new IllegalArgumentException("Serialization offset may not be negative");

        if (serializedHeaders == null)
            throw new IllegalStateException("Must call serializeAndCacheHeaders() before serialization");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            int bytesWritten = 0;

            // Write SessionMessage header version if offset dictates
            if (offset + bytesWritten < HEADER_LENGTH_BYTES) {

                outputStream.write((byte) CURRENT_HEADER_VERSION);

                bytesWritten += HEADER_VERSION_BYTES;
            }

            // Write SessionMessage header length if offset dictates
            if (offset + bytesWritten < HEADER_LENGTH_BYTES) {

                ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.SIZE / 8)
                                                    .order(ByteOrder.LITTLE_ENDIAN)
                                                    .putInt(serializedHeaders.length);

                byte[] truncatedLength = new byte[HEADER_LENGTH_BYTES];
                lengthBuffer.rewind();
                // limit little endian value by ignoring last (most significant) bits
                lengthBuffer.get(truncatedLength);
                outputStream.write(truncatedLength);

                bytesWritten += HEADER_LENGTH_BYTES;
            }

            // Write SessionMessage HashMap header if offset dictates
            if (offset + bytesWritten >= HEADER_LENGTH_BYTES + HEADER_VERSION_BYTES &&
                offset + bytesWritten < serializedHeaders.length) {

                int headerBytesToCopy = Math.min(length - bytesWritten,
                                                 serializedHeaders.length - (bytesWritten + offset - (HEADER_LENGTH_BYTES + HEADER_VERSION_BYTES)));

                outputStream.write(serializedHeaders,
                                   offset + bytesWritten - (HEADER_LENGTH_BYTES + HEADER_VERSION_BYTES),
                                   headerBytesToCopy);

                bytesWritten += headerBytesToCopy;
            }

            // Write raw body if offset dictates
            if (bytesWritten < length && status == Status.COMPLETE) {
                // If no non-body data was written and there is no body, return null
                if (getBodyLengthBytes() == 0 && bytesWritten == 0)
                    return null;

                int bodyOffset = Math.max(0,
                                          offset - (HEADER_LENGTH_BYTES +
                                                    HEADER_VERSION_BYTES +
                                                    serializedHeaders.length)
                );

                byte[] body = getBodyAtOffset(bodyOffset, length - bytesWritten);

                if (body != null)
                    outputStream.write(body);
            }

        } catch (IOException e) {
            Timber.e(e, "IOException while serializing SessionMessage");
        }

        byte[] result = outputStream.toByteArray();
        //Timber.d(String.format("Serialized %d SessionMessage bytes", result.length));
        // Do not return zero length byte[]. Use null to represent no more data
        return result.length == 0 ? null : result;
    }

    /**
     * Serialize the entire message in one go. Must only be used for messages less than 2 MB.
     */
    public byte[] serialize() {
        if (getTotalLengthBytes() > Integer.MAX_VALUE)
            Timber.e("Message too long for serialize! Will be truncated");

        return serialize(0, (int) getTotalLengthBytes());
    }

    /**
     * @return the length of the total SessionMessage in bytes
     */
    public long getTotalLengthBytes() {

        return HEADER_VERSION_BYTES +
               HEADER_LENGTH_BYTES +
               serializedHeaders.length +
               getBodyLengthBytes();
    }

    /**
     * Cache the serialized representation of {@link #headers}.
     * Must be called before {@link #serialize()} or {@link #serialize(int, int)}
     */
    protected void serializeAndCacheHeaders() {
        if (serializedHeaders == null) {
            if (headers == null) headers = populateHeaders();
            JSONObject jsonHeaders = new JSONObject(headers);
            serializedHeaders = jsonHeaders.toString().getBytes();

        }
    }

    @Override
    public int hashCode() {
        // If we only target API 19+, we can move to java.util.Objects.hash
        return Objects.hashCode(headers.get(HEADER_TYPE),
                                headers.get(HEADER_BODY_LENGTH),
                                headers.get(HEADER_ID),
                                headers.get(HEADER_ALIAS));
    }

    @Override
    public boolean equals(Object obj) {

        if(obj == this) return true;
        if(obj == null) return false;

        if (getClass().equals(obj.getClass()))
        {
            final SessionMessage other = (SessionMessage) obj;
            // If we only target API 19+, we can move to the java.util.Objects.equals
            return Objects.equal(getHeaders().get(HEADER_TYPE),
                                 other.getHeaders().get(HEADER_TYPE)) &&
                   Objects.equal(getHeaders().get(HEADER_BODY_LENGTH),
                                 other.getHeaders().get(HEADER_BODY_LENGTH)) &&
                   Objects.equal(getHeaders().get(HEADER_ID),
                                 other.getHeaders().get(HEADER_ID)) &&
                   Objects.equal(getHeaders().get(HEADER_ALIAS),
                            other.getHeaders().get(HEADER_ALIAS));
        }

        return false;
    }

}
