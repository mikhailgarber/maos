package mg.maos;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ObjectTypes {

	private static int INTSIZE = 18;
	private static String INTFORMAT = "%" + INTSIZE + "d";
	private static int DOUBLEPRECISION = 8;
	private static double MULTIPLIER = Math.pow(10d, DOUBLEPRECISION);
	private static long RANGE = (long) Math.pow(10L, INTSIZE);

	
	private static List<String> VALID_TYPES = Arrays.asList(new String[] { String.class.getName(), Long.class.getName(), Double.class.getName(), Date.class.getName() });
	private static List<String> VALID_FACET_TYPES = Arrays.asList(new String[] { String.class.getName()});
	
	private static List<ToStorableInterface> CONVERTERS = Arrays.asList(new ToStorableInterface[] { new ObjectTypes().new StringConverter(), new ObjectTypes().new LongConverter(), new ObjectTypes().new DoubleConverter(), new ObjectTypes().new DateConverter() });

	private ObjectTypes() {
		super();
	}

	protected static void validateType(Object value) {
		validateType(value, false);
	}
	
	protected static void validateType(Object value, boolean isFacet) {
		if (value == null)
			throw new IllegalArgumentException("null value is provided");
		String type = value.getClass().getName();
		if (!VALID_TYPES.contains(type))
			throw new IllegalArgumentException("this type is not supported");
		if(isFacet && !VALID_FACET_TYPES.contains(type))
			throw new IllegalArgumentException("faceting of this type is not supported");
	}

	protected static String toStorable(Object o) {
		int pos = getPos(o.getClass().getName());
		ToStorableInterface converter = CONVERTERS.get(pos);
		return converter.toStorable(o);
	}
	
	protected static Object fromStorable(String s, String type) {
		int pos = getPos(type);
		ToStorableInterface converter = CONVERTERS.get(pos);
		return converter.fromStorable(s);
	}

	private static int getPos(String type) {
		int pos = VALID_TYPES.indexOf(type);
		if(pos == -1) throw new IllegalArgumentException("invalid type:" + type);
		return pos;
	}

	protected static String toStorable(String s) {
		return s;
	}

	protected static String toStorable(Long l) {
		if (Math.abs(l) > RANGE)
			throw new IllegalArgumentException("value is out of range");
		return String.format(INTFORMAT, l + RANGE);
	}

	protected static String toStorable(Double d) {
		return toStorable((long) (MULTIPLIER * d));
	}

	protected static String toStorable(Date d) {
		return toStorable(d.getTime());
	}

	protected static Long toLong(String s) {
		return (Long.parseLong(s.trim()) - RANGE);
	}

	protected static Double toDouble(String s) {
		Long l = toLong(s);
		return l / MULTIPLIER;
	}

	protected static Date toDate(String s) {
		Long l = toLong(s);
		return new Date(l);
	}

	private interface ToStorableInterface {
		public String toStorable(Object o);
		public Object fromStorable(String s);
	}

	private class StringConverter implements ToStorableInterface {

		public String toStorable(Object o) {
			return ObjectTypes.toStorable((String) o);
		}

		public Object fromStorable(String s) {
			return s;
		}

	}

	private class DateConverter implements ToStorableInterface {

		public String toStorable(Object o) {
			return ObjectTypes.toStorable((Date) o);
		}

		public Object fromStorable(String s) {
			return toDate(s);
		}

	}

	private class LongConverter implements ToStorableInterface {

		public String toStorable(Object o) {
			return ObjectTypes.toStorable((Long) o);
		}

		public Object fromStorable(String s) {
			return toLong(s);
		}

	}

	private class DoubleConverter implements ToStorableInterface {

		public String toStorable(Object o) {
			return ObjectTypes.toStorable((Double) o);
		}

		public Object fromStorable(String s) {
			return toDouble(s);
		}

	}

}
