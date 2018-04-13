/*
 * Copyright 2018 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.example.loan;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.impl.DatabaseClientImpl;
import com.marklogic.client.impl.HandleAccessor;
import com.marklogic.client.impl.HandleImplementation;
import com.marklogic.client.io.*;
import com.marklogic.client.io.marker.*;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DBFunctionBase {
   // NOTE: the HTTP library must be fully encapsulated so a different
   // library or even protocol could be substituted without forcing
   // regeneration of the dbfimpl libraries

// TODO:  long term, push down into Java API?

   private final static String UTF8_ID = StandardCharsets.UTF_8.toString();

   private final static MediaType URLENCODED_MIME_TYPE =
         MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");

   private static ObjectMapper mapper = null;

   private DatabaseClient db;

   private OkHttpClient client;
   private HttpUrl baseUrl;

   protected DBFunctionBase(DatabaseClient db) {
      this.db       = db;
      this.client = (OkHttpClient) db.getClientImplementation();

      DatabaseClientImpl dbImpl = (DatabaseClientImpl) db;

// TODO:  baseUrl for mocking
      this.baseUrl = new HttpUrl.Builder()
// TODO:  scheme from security context
//            .scheme(dbImpl.getSecurityContext().getSSLContext() == null ? "http" : "https")
            .scheme("http")
            .host(dbImpl.getHost())
            .port(dbImpl.getPort())
            .encodedPath("/")
            .build();
   }

   // converters
   protected String toDateString(Calendar value) {
      if (value == null) {
         return null;
      }
      return DatatypeConverter.printDate(value);
   }
   protected String toDateString(LocalDate value) {
      if (value == null) {
         return null;
      }
      return value.toString();
   }
   protected String toDateString(XMLGregorianCalendar value) {
      if (value == null) {
         return null;
      } else if (value.getXMLSchemaType() != DatatypeConstants.DATE) {
         throw new IllegalArgumentException(
            "argument datatype should be date instead of: "+value.getXMLSchemaType()
            );
      }
      return value.toXMLFormat();
   }
   protected String toDateTimeString(Calendar value) {
      if (value == null) {
         return null;
      }
      return DatatypeConverter.printDateTime(value);
   }
   protected String toDateTimeString(OffsetDateTime value) {
      if (value == null) {
         return null;
      }
      return value.toString();
   }
   protected String toDateTimeString(XMLGregorianCalendar value) {
      if (value == null) {
         return null;
      } else if (value.getXMLSchemaType() != DatatypeConstants.DATETIME) {
         throw new IllegalArgumentException(
               "argument datatype should be datetime instead of: "+value.getXMLSchemaType()
         );
      }
      return value.toXMLFormat();
   }
   protected String toDayTimeDurationString(java.time.Duration value) {
      if (value == null) {
         return null;
      }
      return value.toString();
   }
   protected String toDayTimeDurationString(javax.xml.datatype.Duration value) {
      if (value == null) {
         return null;
      }
      // TODO: verify xs:dayTimeDuration instead of xs:yearMonthDuration
      return value.toString();
   }
   protected String toDecimalString(BigDecimal value) {
      if (value == null) {
         return null;
      }
      return DatatypeConverter.printDecimal(value);
   }
   protected String toDecimalString(BigInteger value) {
      if (value == null) {
         return null;
      }
      return DatatypeConverter.printInteger(value);
   }
   protected String toTimeString(Calendar value) {
      if (value == null) {
         return null;
      }
      return DatatypeConverter.printTime(value);
   }
   protected String toTimeString(OffsetTime value) {
      if (value == null) {
         return null;
      }
      return value.toString();
   }
   protected String toTimeString(XMLGregorianCalendar value) {
      if (value == null) {
         return null;
      } else if (value.getXMLSchemaType() != DatatypeConstants.TIME) {
         throw new IllegalArgumentException(
               "argument datatype should be time instead of: "+value.getXMLSchemaType()
         );
      }
      return value.toXMLFormat();
   }
   // note that DatatypeConverter also supports xs:base64Binary and xs:hexBinary

   protected BytesHandle toHandle(byte[] content) {
      if (content == null) {
         return null;
      }
      return new BytesHandle(content);
   }
   protected DOMHandle toHandle(Document content) {
      if (content == null) {
         return null;
      }
      return new DOMHandle(content);
   }
   protected FileHandle toHandle(File content) {
      if (content == null) {
         return null;
      }
      return new FileHandle(content);
   }
   protected InputStreamHandle toHandle(InputStream content) {
      if (content == null) {
         return null;
      }
      return new InputStreamHandle(content);
   }
   protected InputSourceHandle toHandle(InputSource content) {
      if (content == null) {
         return null;
      }
      return new InputSourceHandle(content);
   }
   protected JacksonHandle toHandle(JsonNode content) {
      if (content == null) {
         return null;
      }
      return new JacksonHandle(content);
   }
   protected JacksonParserHandle toHandle(JsonParser content) {
      if (content == null) {
         return null;
      }
      return new JacksonParserHandle();
   }
   protected OutputStreamHandle toHandle(OutputStreamSender content) {
      if (content == null) {
         return null;
      }
      return new OutputStreamHandle(content);
   }
   protected ReaderHandle toHandle(Reader content) {
      if (content == null) {
         return null;
      }
      return new ReaderHandle(content);
   }
   protected StringHandle toHandle(String content) {
      if (content == null) {
         return null;
      }
      return new StringHandle(content);
   }
   protected SourceHandle toHandle(Source content) {
      if (content == null) {
         return null;
      }
      return new SourceHandle(content);
   }
   protected XMLEventReaderHandle toHandle(XMLEventReader content) {
      if (content == null) {
         return null;
      }
      return new XMLEventReaderHandle(content);
   }
   protected XMLStreamReaderHandle toHandle(XMLStreamReader content) {
      if (content == null) {
         return null;
      }
      return new XMLStreamReaderHandle(content);
   }

   protected BinaryWriteHandle asBinary(BinaryWriteHandle content) {
      return (BinaryWriteHandle) withFormat(content, Format.BINARY);
   }
   protected JSONWriteHandle asJson(JSONWriteHandle content) {
      return (JSONWriteHandle) withFormat(content, Format.JSON);
   }
   protected TextWriteHandle asText(TextWriteHandle content) {
      return (TextWriteHandle) withFormat(content, Format.TEXT);
   }
   protected XMLWriteHandle asXml(XMLWriteHandle content) {
      return (XMLWriteHandle) withFormat(content, Format.XML);
   }
   protected AbstractWriteHandle withFormat(AbstractWriteHandle content, Format format) {
      if (content != null) {
         if (!(content instanceof BaseHandle)) {
            throw new IllegalArgumentException(
               "cannot set format on handle of "+content.getClass().getName()
               );
         }
         ((BaseHandle) content).setFormat(format);
      }
      return content;
   }

   protected String asString(boolean value) {
      return DatatypeConverter.printBoolean(value);
   }
   protected String asString(double value) {
      return DatatypeConverter.printDouble(value);
   }
   protected String asString(float value) {
      return DatatypeConverter.printFloat(value);
   }
   protected String asString(int value) {
      return DatatypeConverter.printInt(value);
   }
   protected String asString(long value) {
      return DatatypeConverter.printLong(value);
   }

// TODO: distinguish the print / send and parse / receive cases
// TODO: in the print / send case, send empty value as empty
// TODO: in the parse / receive case, distinguish the empty string and null in receiveTextImpl()
   protected String asString(String value) {
      if (value == null || value.length() == 0) {
         return null;
      }
      return DatatypeConverter.printString(value);
   }

   protected String asStringUnsigned(int value) {
      return Integer.toUnsignedString(value);
   }
   protected String asStringUnsigned(long value) {
      return Long.toUnsignedString(value);
   }

   protected String[] asString(boolean[] values) {
      String[] result = new String[values.length];
      for (int i=0; i < values.length; i++) {
         result[i] = asString(values[i]);
      }
      return result;
   }
   protected String[] asString(double[] values) {
      return (String[])
            Arrays.stream(values).mapToObj(value -> asString(value)).toArray(size -> new String[size]);
   }
   protected String[] asString(float[] values) {
      String[] result = new String[values.length];
      for (int i=0; i < values.length; i++) {
         result[i] = asString(values[i]);
      }
      return result;
   }
   protected String[] asString(int[] values) {
      return (String[])
            Arrays.stream(values).mapToObj(value -> asString(value)).toArray(size -> new String[size]);
   }
   protected String[] asString(long[] values) {
      return (String[])
            Arrays.stream(values).mapToObj(value -> asString(value)).toArray(size -> new String[size]);
   }
   protected String[] asStringUnsigned(int[] values) {
      return (String[])
            Arrays.stream(values).mapToObj(value -> asStringUnsigned(value)).toArray(size -> new String[size]);
   }
   protected String[] asStringUnsigned(long[] values) {
      return (String[])
            Arrays.stream(values).mapToObj(value -> asStringUnsigned(value)).toArray(size -> new String[size]);
   }


   // value encoders
   protected String paramEncoded(String paramName, boolean isNullable, Boolean value) {
      if (isParamNull(paramName, isNullable, value)) {
         return null;
      }
      return paramEncoded(paramName, value.booleanValue());
   }
   protected String paramEncoded(String paramName, boolean value) {
      return encodeParamValue(paramName, asString(value));
   }
   protected String paramEncoded(String paramName, boolean isNullable, Double value) {
      if (isParamNull(paramName, isNullable, value)) {
         return null;
      }
      return paramEncoded(paramName, value.doubleValue());
   }
   protected String paramEncoded(String paramName, double value) {
      return encodeParamValue(paramName, asString(value));
   }
   protected String paramEncoded(String paramName, boolean isNullable, Float value) {
      if (isParamNull(paramName, isNullable, value)) {
         return null;
      }
      return paramEncoded(paramName, value.floatValue());
   }
   protected String paramEncoded(String paramName, float value) {
      return encodeParamValue(paramName, asString(value));
   }
   protected String paramEncoded(String paramName, boolean isNullable, Integer value) {
      if (isParamNull(paramName, isNullable, value)) {
         return null;
      }
      return paramEncoded(paramName, value.intValue());
   }
   protected String paramEncoded(String paramName, int value) {
      return encodeParamValue(paramName, asString(value));
   }
   protected String paramEncoded(String paramName, boolean isNullable, Long value) {
      if (isParamNull(paramName, isNullable, value)) {
         return null;
      }
      return paramEncoded(paramName, value.longValue());
   }
   protected String paramEncoded(String paramName, long value) {
      return encodeParamValue(paramName, asString(value));
   }
// TODO: support all atomics by converters?
   // used with converters
   protected String paramEncoded(String paramName, boolean isNullable, String value) {
      if (isParamNull(paramName, isNullable, value)) {
         return null;
      }
      return encodeParamValue(paramName, asString(value));
   }
   protected String paramEncodedUnsigned(String paramName, boolean isNullable, Integer value) {
      if (isParamNull(paramName, isNullable, value)) {
         return null;
      }
      return paramEncodedUnsigned(paramName, value.intValue());
   }
   protected String paramEncodedUnsigned(String paramName, int value) {
      return encodeParamValue(paramName, asStringUnsigned(value));
   }
   protected String paramEncodedUnsigned(String paramName, boolean isNullable, Long value) {
      if (isParamNull(paramName, isNullable, value)) {
         return null;
      }
      return paramEncodedUnsigned(paramName, value.longValue());
   }
   protected String paramEncodedUnsigned(String paramName, long value) {
      return encodeParamValue(paramName, asStringUnsigned(value));
   }
   protected String encodeParamValue(String paramName, String value) {
      if (value == null) {
         return null;
      }
      try {
         return paramName+"="+URLEncoder.encode(value, UTF8_ID);
      } catch(UnsupportedEncodingException e) {
// TODO: library error
         throw new RuntimeException(e);
      }
   }

   // value list joiners
   protected String paramEncoded(String paramName, boolean isNullable, boolean[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      StringJoiner joiner = new StringJoiner("&");
      for (boolean value: values) {
         joiner.add(paramEncoded(paramName, value));
      }
      return joiner.toString();
   }
   protected String paramEncoded(String paramName, boolean isNullable, double[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return (String) Arrays.stream(values)
            .mapToObj(value -> paramEncoded(paramName, value))
            .collect(Collectors.joining("&"));
   }
   protected String paramEncoded(String paramName, boolean isNullable, float[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      StringJoiner joiner = new StringJoiner("&");
      for (float value: values) {
         joiner.add(paramEncoded(paramName, value));
      }
      return joiner.toString();
   }
   protected String paramEncoded(String paramName, boolean isNullable, int[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return (String) Arrays.stream(values)
            .mapToObj(value -> paramEncoded(paramName, value))
            .collect(Collectors.joining("&"));
   }
   protected String paramEncoded(String paramName, boolean isNullable, long[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return (String) Arrays.stream(values)
            .mapToObj(value -> paramEncoded(paramName, value))
            .collect(Collectors.joining("&"));
   }
   // used with converters
   protected String paramEncoded(String paramName, boolean isNullable, String[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return (String) Arrays.stream(values)
            .map(value -> paramEncoded(paramName, isNullable, value))
            .collect(Collectors.joining("&"));
   }
   protected String paramEncodedUnsigned(String paramName, boolean isNullable, int[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return (String) Arrays.stream(values)
            .mapToObj(value -> paramEncodedUnsigned(paramName, value))
            .collect(Collectors.joining("&"));
   }
   protected String paramEncodedUnsigned(String paramName, boolean isNullable, long[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return (String) Arrays.stream(values)
            .mapToObj(value -> paramEncodedUnsigned(paramName, value))
            .collect(Collectors.joining("&"));
   }

   protected PartParam paramPart(String paramName, boolean value) {
      return paramPart(paramName, false, asString(value));
   }
   protected PartParam paramPart(String paramName, double value) {
      return paramPart(paramName, false, asString(value));
   }
   protected PartParam paramPart(String paramName, float value) {
      return paramPart(paramName, false, asString(value));
   }
   protected PartParam paramPart(String paramName, int value) {
      return paramPart(paramName, false, asString(value));
   }
   protected PartParam paramPart(String paramName, long value) {
      return paramPart(paramName, false, asString(value));
   }
   protected PartParam paramPartUnsigned(String paramName, int value) {
      return paramPart(paramName, false, asStringUnsigned(value));
   }
   protected PartParam paramPartUnsigned(String paramName, long value) {
      return paramPart(paramName, false, asStringUnsigned(value));
   }

// TODO: construct encapsulate MultipartBody, chaining the addition of parts instead of interim DocumentParam
   protected PartParam paramPart(String paramName, boolean isNullable, boolean[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return new AtomicParam(paramName, asString(values));
   }
   protected PartParam paramPart(String paramName, boolean isNullable, double[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return new AtomicParam(paramName, asString(values));
   }
   protected PartParam paramPart(String paramName, boolean isNullable, float[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return new AtomicParam(paramName, asString(values));
   }
   protected PartParam paramPart(String paramName, boolean isNullable, int[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return new AtomicParam(paramName, asString(values));
   }
   protected PartParam paramPart(String paramName, boolean isNullable, long[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return new AtomicParam(paramName, asString(values));
   }
   protected PartParam paramPartUnsigned(String paramName, boolean isNullable, int[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return new AtomicParam(paramName, asStringUnsigned(values));
   }
   protected PartParam paramPartUnsigned(String paramName, boolean isNullable, long[] values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return new AtomicParam(paramName, asStringUnsigned(values));
   }
   protected PartParam paramPart(String paramName, boolean isNullable, String... values) {
      if (isParamEmpty(paramName, isNullable, (values == null) ? 0 : values.length)) {
         return null;
      }
      return new AtomicParam(paramName, values);
   }

   protected PartParam paramPart(String paramName, boolean isNullable, AbstractWriteHandle... content) {
      if (isParamEmpty(paramName, isNullable, (content == null) ? 0 : content.length)) {
         return new DocumentParam(paramName, null);
      }
      return new DocumentParam(paramName, content);
   }
   protected PartParam paramPart(String paramName, boolean isNullable, Iterable<AbstractWriteHandle> content) {
      if (content instanceof Collection) {
         return paramPart(paramName, isNullable, (Collection<AbstractWriteHandle>) content);
      }
      return paramPart(paramName, isNullable, (Stream<AbstractWriteHandle>) (
            (content == null) ? content : StreamSupport.stream(content.spliterator(),false)
         ));
   }
   protected PartParam paramPart(String paramName, boolean isNullable, Collection<AbstractWriteHandle> content) {
      int contentSize = (content == null) ? 0 : content.size();
      if (isParamEmpty(paramName, isNullable, contentSize)) {
         return new DocumentParam(paramName, null);
      }
      return new DocumentParam(paramName, content.toArray(new AbstractWriteHandle[contentSize]));
   }
   protected PartParam paramPart(String paramName, boolean isNullable, Stream<AbstractWriteHandle> content) {
      AbstractWriteHandle[] handles =
            (content == null) ? null : content.toArray(size -> new AbstractWriteHandle[size]);
      return paramPart(paramName, isNullable, handles);
   }

   // atomic param joiner
   protected String urlencodedParams(String... urlencodedParams) {
      if (urlencodedParams.length == 0) {
         return null;
      }
      return (String) Arrays.stream(urlencodedParams)
            .filter(urlencodedParam -> urlencodedParam != null)
            .collect(Collectors.joining("&"));
   }
   // document param joiner
   protected PartParam[] partParams(PartParam... iteratorParams) {
      return iteratorParams;
   }

   private boolean isParamNull(String paramName, boolean isNullable, Object value) {
      if (value != null) {
         return false;
      } else if (!isNullable) {
         throw new IllegalArgumentException("null value for required parameter: " + paramName);
      }
      return true;
   }
   private boolean isParamEmpty(String paramName, boolean isNullable, int length) {
      if (length > 0) {
         return false;
      } else if (!isNullable) {
         throw new IllegalArgumentException("empty value list for required parameter: "+paramName);
      }
      return true;
   }

// TODO: core typed conversions - Calendar etc

   protected BigDecimal asBigDecimal(String value) {
      if (value == null || value.length() == 0) {
         return null;
      }
      return DatatypeConverter.parseDecimal(value);
   }
   protected Boolean asNullableBoolean(String value) {
      if (value == null || value.length() == 0) {
         return null;
      }
      return Boolean.valueOf(value);
   }
   protected boolean asBoolean(String value) {
      if (value == null || value.length() == 0) {
         throw new RuntimeException("Returned null for required boolean");
      }
      return DatatypeConverter.parseBoolean(value);
   }
   protected Double asNullableDouble(String value) {
      if (value == null || value.length() == 0) {
         return null;
      }
      return Double.valueOf(value);
   }
   protected double asDouble(String value) {
      if (value == null || value.length() == 0) {
         throw new RuntimeException("Returned null for required double");
      }
      return DatatypeConverter.parseDouble(value);
   }
   protected Float asNullableFloat(String value) {
      if (value == null || value.length() == 0) {
         return null;
      }
      return Float.valueOf(value);
   }
   protected float asFloat(String value) {
      if (value == null || value.length() == 0) {
         throw new RuntimeException("Returned null for required float");
      }
      return DatatypeConverter.parseFloat(value);
   }
   protected Integer asNullableInteger(String value) {
      if (value == null || value.length() == 0) {
         return null;
      }
      return Integer.valueOf(value);
   }
   protected int asInteger(String value) {
      if (value == null || value.length() == 0) {
         throw new RuntimeException("Returned null for required int");
      }
      return DatatypeConverter.parseInt(value);
   }
   protected Long asNullableLong(String value) {
      if (value == null || value.length() == 0) {
         return null;
      }
      return Long.valueOf(value);
   }
   protected long asLong(String value) {
      if (value == null || value.length() == 0) {
         throw new RuntimeException("Returned null for required long");
      }
      return DatatypeConverter.parseLong(value);
   }
   protected Integer asNullableUnsignedInteger(String value) {
      if (value == null || value.length() == 0) {
         return null;
      }
      return Integer.parseUnsignedInt(value);
   }
   protected int asUnsignedInteger(String value) {
      return Integer.parseUnsignedInt(value);
   }
   protected Long asNullableUnsignedLong(String value) {
      if (value == null || value.length() == 0) {
         return null;
      }
      return Long.parseUnsignedLong(value);
   }
   protected long asUnsignedLong(String value) {
      return Long.parseUnsignedLong(value);
   }

   protected BigDecimal[] asArrayOfBigDecimal(Stream<String> values) {
      if (values == null) {
         return new BigDecimal[0];
      }
      return values.map(BigDecimal::new).toArray(size -> new BigDecimal[size]);
   }
   protected boolean[] asArrayOfBoolean(Stream<String> values) {
      if (values == null) {
         return new boolean[0];
      }
      Boolean[] converted = values.map(DatatypeConverter::parseBoolean).toArray(size -> new Boolean[size]);
      int max = converted.length;
      boolean[] ret = new boolean[max];
      for (int i=0; i<max; i++) {
         ret[i] = converted[i].booleanValue();
      }
      return ret;
   }
   protected double[] asArrayOfDouble(Stream<String> values) {
      if (values == null) {
         return new double[0];
      }
      return values.mapToDouble(DatatypeConverter::parseDouble).toArray();
   }
   protected float[] asArrayOfFloat(Stream<String> values) {
      if (values == null) {
         return new float[0];
      }
      Float[] converted = values.map(DatatypeConverter::parseFloat).toArray(size -> new Float[size]);
      int max = converted.length;
      float[] ret = new float[max];
      for (int i=0; i<max; i++) {
         ret[i] = converted[i].floatValue();
      }
      return ret;
   }
   protected int[] asArrayOfInteger(Stream<String> values) {
      if (values == null) {
         return new int[0];
      }
      return values.mapToInt(DatatypeConverter::parseInt).toArray();
   }
   protected long[] asArrayOfLong(Stream<String> values) {
      if (values == null) {
         return new long[0];
      }
      return values.mapToLong(DatatypeConverter::parseLong).toArray();
   }
   protected String[] asArrayOfString(Stream<String> values) {
      if (values == null) {
         return new String[0];
      }
      return values.toArray(size -> new String[size]);
   }
   protected int[] asArrayOfUnsignedInteger(Stream<String> values) {
      if (values == null) {
         return new int[0];
      }
      return values.mapToInt(Integer::parseUnsignedInt).toArray();
   }
   protected long[] asArrayOfUnsignedLong(Stream<String> values) {
      if (values == null) {
         return new long[0];
      }
      return values.mapToLong(Long::parseUnsignedLong).toArray();
   }

   protected List<BigDecimal> asListOfBigDecimal(Stream<BigDecimal> values) {
      if (values == null) {
         return new ArrayList<>();
      }
      return values.collect(Collectors.toList());
   }
   protected List<Boolean> asListOfBoolean(Stream<Boolean> values) {
      if (values == null) {
         return new ArrayList<>();
      }
      return values.collect(Collectors.toList());
   }
   protected List<Double> asListOfDouble(Stream<Double> values) {
      if (values == null) {
         return new ArrayList<>();
      }
      return values.collect(Collectors.toList());
   }
   protected List<Float> asListOfFloat(Stream<Float> values) {
      if (values == null) {
         return new ArrayList<>();
      }
      return values.collect(Collectors.toList());
   }
   protected List<Integer> asListOfInteger(Stream<Integer> values) {
      if (values == null) {
         return new ArrayList<>();
      }
      return values.collect(Collectors.toList());
   }
   protected List<Long> asListOfLong(Stream<Long> values) {
      if (values == null) {
         return new ArrayList<>();
      }
      return values.collect(Collectors.toList());
   }
   protected List<String> asListOfString(Stream<String> values) {
      if (values == null) {
         return new ArrayList<>();
      }
      return values.collect(Collectors.toList());
   }
   protected List<Integer> asListOfUnsignedInteger(Stream<Integer> values) {
      if (values == null) {
         return new ArrayList<>();
      }
      return values.collect(Collectors.toList());
   }
   protected List<Long> asListOfUnsignedLong(Stream<Long> values) {
      if (values == null) {
         return new ArrayList<>();
      }
      return values.collect(Collectors.toList());
   }

   protected Stream<BigDecimal> asStreamOfBigDecimal(Stream<String> values) {
      if (values == null) {
         return Stream.empty();
      }
      return values.map(DatatypeConverter::parseDecimal);
   }
   protected Stream<Boolean> asStreamOfBoolean(Stream<String> values) {
      if (values == null) {
         return Stream.empty();
      }
      return values.map(DatatypeConverter::parseBoolean);
   }
   protected Stream<Double> asStreamOfDouble(Stream<String> values) {
      if (values == null) {
         return Stream.empty();
      }
      return values.map(DatatypeConverter::parseDouble);
   }
   protected Stream<Float> asStreamOfFloat(Stream<String> values) {
      if (values == null) {
         return Stream.empty();
      }
      return values.map(DatatypeConverter::parseFloat);
   }
   protected Stream<Integer> asStreamOfInteger(Stream<String> values) {
      if (values == null) {
         return Stream.empty();
      }
      return values.map(DatatypeConverter::parseInt);
   }
   protected Stream<Long> asStreamOfLong(Stream<String> values) {
      if (values == null) {
         return Stream.empty();
      }
      return values.map(DatatypeConverter::parseLong);
   }
   protected Stream<Integer> asStreamOfUnsignedInteger(Stream<String> values) {
      if (values == null) {
         return Stream.empty();
      }
      return values.map(Integer::parseUnsignedInt);
   }
   protected Stream<Long> asStreamOfUnsignedLong(Stream<String> values) {
      if (values == null) {
         return Stream.empty();
      }
      return values.map(Long::parseUnsignedLong);
   }

// TODO: or okio.BufferedSource?
   protected InputStream asInputStream(ResponseBody body, Format format, boolean isNullable) {
      if (checkNull(body, format, isNullable)) {
         return null;
      }
      return body.byteStream();
   }
   protected Reader asReader(ResponseBody body, Format format, boolean isNullable) {
      if (checkNull(body, format, isNullable)) {
         return null;
      }
      return body.charStream();
   }

   protected InputStream asInputStream(MimeMultipart multipart, Format format, boolean isNullable) {
      try {
         int partCount = (checkNull(multipart, format, isNullable)) ? 0 : multipart.getCount();
         switch(partCount) {
         case 0:
            return null;
         case 1:
            return asInputStream(multipart.getBodyPart(0));
         default:
            throw new RuntimeException("Received multiple documents");
         }
      } catch (MessagingException e) {
         throw new RuntimeException(e);
      }
   }
   protected Reader asReader(MimeMultipart multipart, Format format, boolean isNullable) {
      InputStream part = asInputStream(multipart, format, isNullable);
      if (part == null) {
         return null;
      }

      try {
         return new InputStreamReader(part, "UTF-8");
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

/* TODO LATER: wrap in converter; maybe Stream of String
    */

   protected Stream<Boolean> asStreamOfBoolean(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfBoolean(asStreamOfString(multipart, isNullable));
   }
   protected Stream<Double> asStreamOfDouble(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfDouble(asStreamOfString(multipart, isNullable));
   }
   protected Stream<Float> asStreamOfFloat(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfFloat(asStreamOfString(multipart, isNullable));
   }
   protected Stream<Integer> asStreamOfInteger(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfInteger(asStreamOfString(multipart, isNullable));
   }
   protected Stream<Long> asStreamOfLong(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfLong(asStreamOfString(multipart, isNullable));
   }
   protected Stream<Integer> asStreamOfUnsignedInteger(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfUnsignedInteger(asStreamOfString(multipart, isNullable));
   }
   protected Stream<Long> asStreamOfUnsignedLong(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfUnsignedLong(asStreamOfString(multipart, isNullable));
   }
   protected Stream<String> asStreamOfString(MimeMultipart multipart, boolean isNullable) {
      try {
         int partCount = (checkNull(multipart, Format.TEXT, isNullable)) ? 0 : multipart.getCount();
         Stream.Builder<String> bldr = Stream.builder();
         for (int i=0; i < partCount; i++) {
            bldr.accept(asString(multipart.getBodyPart(i)));
         }
         return bldr.build();
      } catch (MessagingException e) {
         throw new RuntimeException(e);
      }
   }

   protected List<Boolean> asListOfBoolean(MimeMultipart multipart, boolean isNullable) {
      return asListOfBoolean(asStreamOfBoolean(multipart, isNullable));
   }
   protected List<Double> asListOfDouble(MimeMultipart multipart, boolean isNullable) {
      return asListOfDouble(asStreamOfDouble(multipart, isNullable));
   }
   protected List<Float> asListOfFloat(MimeMultipart multipart, boolean isNullable) {
      return asListOfFloat(asStreamOfFloat(multipart, isNullable));
   }
   protected List<Integer> asListOfInteger(MimeMultipart multipart, boolean isNullable) {
      return asListOfInteger(asStreamOfInteger(multipart, isNullable));
   }
   protected List<Long> asListOfLong(MimeMultipart multipart, boolean isNullable) {
      return asListOfLong(asStreamOfLong(multipart, isNullable));
   }
   protected List<Integer> asListOfUnsignedInteger(MimeMultipart multipart, boolean isNullable) {
      return asListOfUnsignedInteger(asStreamOfUnsignedInteger(multipart, isNullable));
   }
   protected List<Long> asListOfUnsignedLong(MimeMultipart multipart, boolean isNullable) {
      return asListOfUnsignedLong(asStreamOfUnsignedLong(multipart, isNullable));
   }
   protected List<String> asListOfString(MimeMultipart multipart, boolean isNullable) {
      return asListOfString(asStreamOfString(multipart, isNullable));
   }

   protected boolean[] asArrayOfBoolean(MimeMultipart multipart, boolean isNullable) {
      return asArrayOfBoolean(asStreamOfString(multipart, isNullable));
   }
   protected double[] asArrayOfDouble(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfDouble(multipart, isNullable).mapToDouble(Double::doubleValue).toArray();
   }
   protected float[] asArrayOfFloat(MimeMultipart multipart, boolean isNullable) {
      return asArrayOfFloat(asStreamOfString(multipart, isNullable));
   }
   protected int[] asArrayOfInteger(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfInteger(multipart, isNullable).mapToInt(Integer::intValue).toArray();
   }
   protected long[] asArrayOfLong(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfLong(multipart, isNullable).mapToLong(Long::longValue).toArray();
   }
   protected int[] asArrayOfUnsignedInteger(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfUnsignedInteger(multipart, isNullable).mapToInt(Integer::intValue).toArray();
   }
   protected long[] asArrayOfUnsignedLong(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfUnsignedLong(multipart, isNullable).mapToLong(Long::longValue).toArray();
   }
   protected String[] asArrayOfString(MimeMultipart multipart, boolean isNullable) {
      return asStreamOfString(multipart, isNullable).toArray(size -> new String[size]);
   }

   protected Reader[] asArrayOfReader(MimeMultipart multipart, Format format, boolean isNullable) {
      try {
         int partCount = (checkNull(multipart, format, isNullable)) ? 0 : multipart.getCount();
         Reader[] parts = new Reader[partCount];
         for (int i=0; i < partCount; i++) {
            parts[i] = asReader(multipart.getBodyPart(i));
         }
         return parts;
      } catch (MessagingException e) {
         throw new RuntimeException(e);
      }
   }
   protected InputStream[] asArrayOfInputStream(MimeMultipart multipart, Format format, boolean isNullable) {
      try {
         int partCount = (checkNull(multipart, format, isNullable)) ? 0 : multipart.getCount();
         InputStream[] parts = new InputStream[partCount];
         for (int i=0; i < partCount; i++) {
            parts[i] = asInputStream(multipart.getBodyPart(i));
         }
         return parts;
      } catch (MessagingException e) {
         throw new RuntimeException(e);
      }
   }

   protected Reader asReader(BodyPart part) {
      try {
         return new InputStreamReader(asInputStream(part), "UTF-8");
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
      }
   }
   protected InputStream asInputStream(BodyPart part) {
      try {
         return part.getInputStream();
      } catch (MessagingException e) {
         throw new RuntimeException(e);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
   protected String asString(BodyPart part) {
      return asString(asReader(part));
   }
   protected String asString(Reader reader) {
      try {
         StringBuilder bldr = new StringBuilder();
         char[] buf = new char[8192];
         int charCount = -1;
         while ((charCount=reader.read(buf)) != -1) {
            bldr.append(buf, 0, charCount);
         }
         return bldr.toString();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected List<Reader> asListOfReader(MimeMultipart multipart, Format format, boolean isNullable) {
      // TODO: lazy list from a lazy multipart parser
      return asStreamOfReader(multipart, format, isNullable).collect(Collectors.toList());
   }
   protected List<InputStream> asListOfInputStream(MimeMultipart multipart, Format format, boolean isNullable) {
      // TODO: lazy list from a lazy multipart parser
      return asStreamOfInputStream(multipart, format, isNullable).collect(Collectors.toList());
   }
   protected Stream<Reader> asStreamOfReader(MimeMultipart multipart, Format format, boolean isNullable) {
      if (checkNull(multipart, format, isNullable)) {
         return Stream.empty();
      }
      return iteratorStream(new ReaderIterator(new InputStreamIterator(new BodyPartIterator(multipart))));
   }
   protected Stream<InputStream> asStreamOfInputStream(MimeMultipart multipart, Format format, boolean isNullable) {
      if (checkNull(multipart, format, isNullable)) {
         return Stream.empty();
      }
      return iteratorStream(new InputStreamIterator(new BodyPartIterator(multipart)));
   }

   protected <T> Stream<T> iteratorStream(Iterator<? extends T> iterator) {
      return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
            false
      );
   }

   protected boolean checkNull(ResponseBody body, Format format, boolean isNullable) {
      if (body != null) {
         if (body.contentLength() == 0) {
            body.close();
         } else {
            MediaType actualType = body.contentType();
            if (actualType == null) {
               body.close();
               throw new RuntimeException(
                     "Returned document with unknown mime type instead of "+format.getDefaultMimetype()
               );
            } else if (!actualType.toString().startsWith(format.getDefaultMimetype())) {
               body.close();
               throw new RuntimeException(
                     "Returned document as "+actualType.toString()+" instead of "+format.getDefaultMimetype()
               );
            }
            return false;
         }
      }
      if (!isNullable) {
         throw new RuntimeException("Returned null for required "+format.name()+" document");
      }
      return true;
   }
   protected boolean checkNull(MimeMultipart multipart, Format format, boolean isNullable) {
      if (multipart != null) {
         try {
            if (multipart.getCount() != 0) {
               BodyPart firstPart  = multipart.getBodyPart(0);
               String   actualType = (firstPart == null) ? null : firstPart.getContentType();
               if (actualType == null || !actualType.startsWith(format.getDefaultMimetype())) {
                  throw new RuntimeException(
                     "Returned document as "+actualType+" instead of "+format.getDefaultMimetype()
                  );
               }
               return false;
            }
         } catch (MessagingException e) {
            new RuntimeException(e);
         }
      }
      if (!isNullable) {
         throw new RuntimeException("Returned null for required "+format.name()+" document list");
      }
      return true;
   }

/*
TODO:
    validate nullability for null value or zero-length value list
    pass in nullability and multiplicity from generated code
    require serviceName?

TODO: serviceName, functionName, and paramName validation during code generation

TODO: UTC considerations for java.time? format(DateTimeFormatter) instead of toString()?

TODO: share with XsValueImpl including adding LocalDate, ...

System.out.println(responseBody.string());

instanceof OutputStreamSender except StringHandle
byte[] InputStream File are not OutputStreamSender String

 */

   protected void postForNone(String endpointPath) {
      Request.Builder requestBldr = makePostRequest(endpointPath);
      receiveNoneImpl(requestBldr);
   }
   protected void postForNone(String endpointPath, String atomics) {
      Request.Builder requestBldr = makePostRequest(endpointPath, atomics);
      receiveNoneImpl(requestBldr);
   }
   protected void postForNone(String endpointPath, AbstractWriteHandle document) {
      Request.Builder requestBldr = makePostRequest(endpointPath, document);
      receiveNoneImpl(requestBldr);
   }
   protected void postForNone(String endpointPath, PartParam[] partParams) {
      Request.Builder requestBldr = makePostRequest(endpointPath, partParams);
      receiveNoneImpl(requestBldr);
   }

   protected String postForText(String endpointPath) {
      Request.Builder requestBldr = forTextResponse(makePostRequest(endpointPath));
      return receiveTextImpl(requestBldr);
   }
   protected String postForText(String endpointPath, String atomics) {
      Request.Builder requestBldr = forTextResponse(makePostRequest(endpointPath, atomics));
      return receiveTextImpl(requestBldr);
   }
   protected String postForText(String endpointPath, AbstractWriteHandle document) {
      Request.Builder requestBldr = forTextResponse(makePostRequest(endpointPath, document));
      return receiveTextImpl(requestBldr);
   }
   protected String postForText(String endpointPath, PartParam[] partParams) {
      Request.Builder requestBldr = forTextResponse(makePostRequest(endpointPath, partParams));
      return receiveTextImpl(requestBldr);
   }

   protected ResponseBody postForDocument(String endpointPath, Format format) {
      Request.Builder requestBldr = forDocumentResponse(makePostRequest(endpointPath), format);
      return receiveBodyImpl(requestBldr);
   }
   protected ResponseBody postForDocument(String endpointPath, String atomics, Format format) {
      Request.Builder requestBldr = forDocumentResponse(makePostRequest(endpointPath, atomics), format);
      return receiveBodyImpl(requestBldr);
   }
   protected ResponseBody postForDocument(String endpointPath, AbstractWriteHandle document, Format format) {
      Request.Builder requestBldr = forDocumentResponse(makePostRequest(endpointPath, document), format);
      return receiveBodyImpl(requestBldr);
   }
   protected ResponseBody postForDocument(String endpointPath, PartParam[] partParams, Format format) {
      Request.Builder requestBldr = forDocumentResponse(makePostRequest(endpointPath, partParams), format);
      return receiveBodyImpl(requestBldr);
   }

   protected MimeMultipart postForMultipart(String endpointPath) {
      Request.Builder requestBldr = forMultipartResponse(makePostRequest(endpointPath));
      return receiveMultipartImpl(requestBldr);
   }
   protected MimeMultipart postForMultipart(String endpointPath, String atomics) {
      Request.Builder requestBldr = forMultipartResponse(makePostRequest(endpointPath, atomics));
      return receiveMultipartImpl(requestBldr);
   }
   protected MimeMultipart postForMultipart(String endpointPath, AbstractWriteHandle document) {
      Request.Builder requestBldr = forMultipartResponse(makePostRequest(endpointPath, document));
      return receiveMultipartImpl(requestBldr);
   }
   protected MimeMultipart postForMultipart(String endpointPath, PartParam[] partParams) {
      Request.Builder requestBldr = forMultipartResponse(makePostRequest(endpointPath, partParams));
      return receiveMultipartImpl(requestBldr);
   }

   private void receiveNoneImpl(Request.Builder requestBldr) {
      receiveImpl(requestBldr);
   }
   private String receiveTextImpl(Request.Builder requestBldr) {
      try {
         Response response = receiveImpl(requestBldr);
         ResponseBody responseBody = response.body();

         return responseBody.string();
      } catch(IOException e) {
// TODO: library error
         throw new RuntimeException(e);
      }
   }
   private ResponseBody receiveBodyImpl(Request.Builder requestBldr) {
      Response response = receiveImpl(requestBldr);
// TODO: inspect nullability?
      return response.body();
   }
   private MimeMultipart receiveMultipartImpl(Request.Builder requestBldr) {
      try {
         Response response = receiveImpl(requestBldr);
// TODO: inspect nullability?
         ResponseBody body = response.body();
         ByteArrayDataSource dataSource = new ByteArrayDataSource(body.byteStream(), body.contentType().toString());
         return new MimeMultipart(dataSource);
      } catch (IOException e) {
         throw new RuntimeException(e);
      } catch (MessagingException e) {
         throw new RuntimeException(e);
      }
   }
   private Response receiveImpl(Request.Builder requestBldr) {
      try {
         Request request = requestBldr.build();
System.out.println("calling "+request.url().toString());
         Response response = client.newCall(request).execute();
         int statusCode = response.code();
System.out.println("code: "+statusCode);
         if (statusCode >= 300) {
            // okay if one thread overwrites another during lazy initialization
            if (mapper == null) {
               mapper = new ObjectMapper();
            }

            ResponseBody errorBody = response.body();
            if (errorBody.contentLength() > 0 &&
                  errorBody.contentType().equals(MediaType.parse("application/vnd.marklogic-error+json"))) {
               ObjectNode errorObj   = mapper.readValue(errorBody.string(), ObjectNode.class);
               JsonNode errMsgProp = errorObj.get("errorMessage");
               String     errMsgText = (errMsgProp != null) ? errMsgProp.asText() : null;
               if (errMsgText != null && errMsgText.length() > 0) {
System.out.println("error: "+errMsgText);
                  throw new IllegalArgumentException(errMsgText);
               }
            }
            throw new RuntimeException("request failed");
         }
         return response;
      } catch (IOException e) {
// TODO: library error
         throw new RuntimeException(e);
      }
   }

   private Request.Builder makeRequest(String endpointPath) {
      HttpUrl url = makeURL(endpointPath);
      return new Request.Builder().url(url);
   }
   private RequestBody makeRequestBody(String value) {
      if (value == null) {
         return new EmptyRequestBody();
      }
      return RequestBody.create(MediaType.parse("text/plain"), value);
   }
   private RequestBody makeRequestBody(AbstractWriteHandle document) {
      if (document == null) {
         return new EmptyRequestBody();
      }
      HandleImplementation handleBase = HandleAccessor.as(document);
      Format format = handleBase.getFormat();
      String mimetype = handleBase.getMimetype();
//      String mimetype = handleBase.getMimetype() +
//            ((format == Format.BINARY) ? "" : ";charset=UTF-8");
      MediaType mediaType = MediaType.parse(mimetype);
      return (document instanceof OutputStreamSender) ?
         new StreamingOutputImpl((OutputStreamSender) document,      mediaType) :
         new ObjectRequestBody(HandleAccessor.sendContent(document), mediaType);
   }
   private RequestBody makeRequestBody(PartParam[] partParams) {
      if (partParams == null || partParams.length == 0) {
         return new EmptyRequestBody();
      }
      MultipartBody.Builder multiBldr = new MultipartBody.Builder();
      multiBldr.setType(MultipartBody.MIXED);
      int multiSize = 0;
      for (PartParam partParam: partParams) {
         if (partParam == null) {
            continue;
         }

         String  paramName          = partParam.getParamName();
         String  contentDisposition = "form-data; name=\""+paramName+"\"";
         Headers headers            = new Headers.Builder()
               .add("Content-Disposition", contentDisposition)
               .build();

         multiSize += partParam.addRequestBodies(multiBldr, headers);
      }
      if (multiSize == 0) {
         return new EmptyRequestBody();
      }
      return multiBldr.build();
   }

   private Request.Builder makePostRequest(String endpointPath) {
      return makeRequest(endpointPath).post(new EmptyRequestBody());
   }
   private Request.Builder makePostRequest(String endpointPath, String atomics) {
      return makeRequest(endpointPath).post(
         RequestBody.create(URLENCODED_MIME_TYPE, (atomics == null) ? "" : atomics)
         );
   }
   private Request.Builder makePostRequest(String endpointPath, AbstractWriteHandle document) {
      return makeRequest(endpointPath).post(makeRequestBody(document));
   }
   private Request.Builder makePostRequest(String endpointPath, PartParam[] partParams) {
      if (partParams == null || partParams.length == 0) {
         return makePostRequest(endpointPath);
      }
      return makeRequest(endpointPath).post(makeRequestBody(partParams));
   }

   private Request.Builder forTextResponse(Request.Builder requestBldr) {
      return requestBldr.addHeader("Accept", "text/plain");
   }
   private Request.Builder forDocumentResponse(Request.Builder requestBldr, Format format) {
      return requestBldr.addHeader("Accept", format.getDefaultMimetype());
   }
   private Request.Builder forMultipartResponse(Request.Builder requestBldr) {
      return requestBldr.addHeader(
            "Accept",
            "multipart/mixed; boundary=\""+UUID.randomUUID().toString()+"\""
         );
   }
   private HttpUrl makeURL(String endpointPath) {
      return baseUrl.resolve(endpointPath);
   }

// TODO: convert to Multipart wrapper
   protected abstract class PartParam {
      private String paramName = null;
      PartParam(String paramName) {
         this.paramName = paramName;
      }
      String getParamName() {
         return paramName;
      }
      abstract int addRequestBodies(MultipartBody.Builder multiBldr, Headers headers);
   }
   protected class AtomicParam extends PartParam {
      private String[] values = null;
      AtomicParam(String paramName, String[] values) {
         super(paramName);
         this.values = (values == null || values.length == 0) ? null : values;
      }
      @Override
      int addRequestBodies(MultipartBody.Builder multiBldr, Headers headers) {
         int multiSize = 0;
         if (values != null) {
            for (String value: values) {
               if (value == null) {
                  continue;
               }
               multiBldr.addPart(headers, makeRequestBody(value));
               multiSize++;
            }
         }
         return multiSize;
      }
   }
   protected class DocumentParam extends PartParam {
      private AbstractWriteHandle[] documents = null;
      DocumentParam(String paramName, AbstractWriteHandle[] documents) {
         super(paramName);
         this.documents = (documents == null || documents.length == 0) ? null : documents;
      }
      @Override
      int addRequestBodies(MultipartBody.Builder multiBldr, Headers headers) {
         int multiSize = 0;
         if (documents != null) {
            for (AbstractWriteHandle document: documents) {
               if (document == null) {
                  continue;
               }
               multiBldr.addPart(headers, makeRequestBody(document));
               multiSize++;
            }
         }
         return multiSize;
      }
   }

   private class EmptyRequestBody extends RequestBody {
      public MediaType contentType() {
         return null;
      }
      public void writeTo(BufferedSink sink) {
      }
   }

   // TODO: use the version provided by OkHttpServices
   class StreamingOutputImpl extends RequestBody {
      private OutputStreamSender handle;
      private MediaType contentType;

      StreamingOutputImpl(OutputStreamSender handle, MediaType contentType) {
         super();
         this.handle = handle;
         this.contentType = contentType;
      }

      @Override
      public MediaType contentType() {
         return contentType;
      }

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
         OutputStream out = sink.outputStream();

         handle.write(out);
         out.flush();
      }
   }

   // TODO: use the version provided by OkHttpServices
   private class ObjectRequestBody extends RequestBody {
      private Object obj;
      private MediaType contentType;

      ObjectRequestBody(Object obj, MediaType contentType) {
         super();
         this.obj = obj;
         this.contentType = contentType;
      }

      @Override
      public MediaType contentType() {
         return contentType;
      }

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
         if ( obj instanceof InputStream ) {
            sink.writeAll(Okio.source((InputStream) obj));
         } else if ( obj instanceof File ) {
            try ( okio.Source source = Okio.source((File) obj) ) {
               sink.writeAll(source);
            }
         } else if ( obj instanceof byte[] ) {
            sink.write((byte[]) obj);
         } else if ( obj instanceof String) {
            sink.writeUtf8((String) obj);
         } else if ( obj == null ) {
         } else {
            throw new IllegalStateException("Cannot write object of type: " + obj.getClass());
         }
      }
   }

   private class BodyPartIterator implements Iterator<BodyPart> {
      int nextPart  = 0;
      int partCount = 0;
      MimeMultipart multipart = null;
      BodyPartIterator(MimeMultipart multipart) {
         this.multipart = multipart;
         try {
            partCount = multipart.getCount();
         } catch (MessagingException e) {
            throw new RuntimeException(e);
         }
      }
      @Override
      public boolean hasNext() {
         return nextPart < partCount;
      }
      @Override
      public BodyPart next() {
         if (!hasNext()) {
            return null;
         }
         try {
            BodyPart bodyPart = multipart.getBodyPart(nextPart);
            nextPart++;
            return bodyPart;
         } catch (MessagingException e) {
            throw new RuntimeException(e);
         }
      }
   }
   private class InputStreamIterator implements Iterator<InputStream> {
      BodyPartIterator bodyPartIterator = null;
      InputStreamIterator(BodyPartIterator bodyPartIterator) {
         this.bodyPartIterator = bodyPartIterator;
      }
      @Override
      public boolean hasNext() {
         return bodyPartIterator.hasNext();
      }
      @Override
      public InputStream next() {
         try {
            BodyPart bodyPart = bodyPartIterator.next();
            return (bodyPart == null) ? null : bodyPart.getInputStream();
         } catch (MessagingException e) {
            throw new RuntimeException(e);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
   }
   private class ReaderIterator implements Iterator<Reader> {
      InputStreamIterator inputStreamIterator = null;
      ReaderIterator(InputStreamIterator inputStreamIterator) {
         this.inputStreamIterator = inputStreamIterator;
      }
      @Override
      public boolean hasNext() {
         return inputStreamIterator.hasNext();
      }
      @Override
      public Reader next() {
         try {
            InputStream inputStream = inputStreamIterator.next();
            return (inputStream == null) ? null : new InputStreamReader(inputStream, "UTF-8");
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
   }
}
