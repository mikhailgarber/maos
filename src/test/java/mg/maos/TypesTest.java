package mg.maos;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

public class TypesTest {

	@Test
	public void testValidTypes() {
		ObjectTypes.validateType(new Long(3));
		ObjectTypes.validateType(new String("3"));
		ObjectTypes.validateType(new Double(3));
		ObjectTypes.validateType(new Date());

	}

	@Test(expected=IllegalArgumentException.class)
	public void testInvalidType() {
		ObjectTypes.validateType(new Integer(3));
	}
	
	@Test
	public void testLong() {
		Long l = 23L;
		doLong(l);
	}

	@Test
	public void testNegativeLong() {
		doLong(-23L);
	}
	
	@Test
	public void testBigLong() {
		doLong((long)(Math.pow(10, 18)));
	}
	
	@Test
	public void testBigNegativeLong() {
		doLong((-1) * (long)(Math.pow(10, 18)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testTooBigLong() {
		doLong(1+(long)(Math.pow(10, 18)));
	}
	
	@Test
	public void testDouble() {
		doDouble(23.456);
	}
	
	@Test
	public void testNegativeDouble() {
		doDouble(-23.456);
	}
	
	@Test
	public void testPreciseDouble() {
		doDouble(23.00000001);
	}
	
	@Test
	public void testTooPreciseDouble() {
		String s = ObjectTypes.toStorable(23.000000001);
		System.out.println(s);
		Assert.assertEquals(23d, ObjectTypes.toDouble(s));
	}
	
	@Test
	public void testDate() {
		Date d = new Date();
		String s = ObjectTypes.toStorable(d);
		System.out.println(s);
		Assert.assertEquals(d, ObjectTypes.toDate(s));
		
	}
	
	private void doDouble(Double d) {
		String s = ObjectTypes.toStorable(d);
		System.out.println(s);
		Assert.assertEquals(d, ObjectTypes.toDouble(s));
		
	}
	
	private void doLong(Long l) {
		String s = ObjectTypes.toStorable(l);
		System.out.println(s);
		Assert.assertEquals(l, ObjectTypes.toLong(s));
	}
}
