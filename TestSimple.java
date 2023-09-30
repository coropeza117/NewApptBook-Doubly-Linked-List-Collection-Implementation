import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import edu.uwm.cs351.Appointment;
import edu.uwm.cs351.Duration;
import edu.uwm.cs351.NewApptBook;
import edu.uwm.cs351.Period;
import edu.uwm.cs351.Time;
import junit.framework.TestCase;


public class TestSimple extends TestCase {
	Time now = new Time();
	Appointment e1 = new Appointment(new Period(now,Duration.HOUR),"1: think");
	Appointment e2 = new Appointment(new Period(now,Duration.DAY),"2: current");
	Appointment e3 = new Appointment(new Period(now.add(Duration.HOUR),Duration.HOUR),"3: eat");
	Appointment e4 = new Appointment(new Period(now.add(Duration.HOUR.scale(2)),Duration.HOUR.scale(8)),"4: sleep");
	Appointment e5 = new Appointment(new Period(now.add(Duration.DAY),Duration.DAY),"5: tomorrow");

	NewApptBook s;
	
	protected NewApptBook newCollection() {
		return new NewApptBook();
	}

	@Override
	protected void setUp() {
		s = newCollection();
		try {
			assert 1/((int)e1.getDescription().length()-8) == 42 : "OK";
			System.err.println("Assertions must be enabled to use this test suite.");
			System.err.println("In Eclipse: add -ea in the VM Arguments box under Run>Run Configurations>Arguments");
			assertFalse("Assertions must be -ea enabled in the Run Configuration>Arguments>VM Arguments",true);
		} catch (ArithmeticException ex) {
			return;
		}
	}

	protected <T> void assertException(Class<?> excClass, Supplier<T> producer) {
		try {
			T result = producer.get();
			assertFalse("Should have thrown an exception, not returned " + result,true);
		} catch (RuntimeException ex) {
			if (!excClass.isInstance(ex)) {
				assertFalse("Wrong kind of exception thrown: "+ ex.getClass().getSimpleName(),true);
			}
		}		
	}

	protected <T> void assertException(Runnable f, Class<?> excClass) {
		try {
			f.run();
			assertFalse("Should have thrown an exception, not returned",true);
		} catch (RuntimeException ex) {
			if (!excClass.isInstance(ex)) {
				assertFalse("Wrong kind of exception thrown: "+ ex.getClass().getSimpleName(),true);
			}
		}		
	}

	/**
	 * Return the Appointment as an integer
	 * <dl>
	 * <dt>-1<dd><i>(an exception was thrown)
	 * <dt>0<dd>null
	 * <dt>1<dd>e1
	 * <dt>2<dd>e2
	 * <dt>3<dd>e3
	 * <dt>4<dd>e4
	 * <dt>5<dd>e5
	 * </dl>
	 * @return integer encoding of Appointment supplied
	 */
	protected int asInt(Supplier<Appointment> g) {
		try {
			Appointment n = g.get();
			if (n == null) return 0;
			return n.getDescription().charAt(0) - '0';
		} catch (RuntimeException ex) {
			return -1;
		}
	}
	
	public void testA() {
		// Nothing added yet:
		assertEquals(0,s.size());
		assertFalse(s.iterator().hasNext());
		assertFalse(s.iterator().hasNext());
	}
	
	public void testB() {
		// Initially empty.
		assertException(NoSuchElementException.class, () -> s.iterator().next());
		s.add(e1);
		assertSame(e1,s.iterator().next());
		Iterator<Appointment> it = s.iterator();
		assertSame(e1, it.next());
		assertException(NoSuchElementException.class, () -> it.next());
	}
	
	public void testC() {
		// Initially empty.
		s.add(e5);
		s.add(e4);
		assertEquals(2,s.size());
		Iterator<Appointment> it = s.iterator();
		// -1 for error, 0 for null, 1 for e1, 2 for e2 ...
		assertSame(e4, it.next());
		assertSame(e5, it.next());
		assertException(NoSuchElementException.class, () -> it.next());
	}
		
	public void testD() {
		s.add(e1);
		Iterator<Appointment> it = s.iterator();
		assertTrue(it.hasNext());
		s.add(e2);
		// -1 for error, 0 for null, 1 for e1, 2 for e2 ...
		assertException(ConcurrentModificationException.class, () -> it.next());
	}
	
	public void testE() {
		// Initially empty
		assertException(NullPointerException.class, () -> s.add(null));
		assertEquals(0,s.size());
		assertEquals(false,s.iterator().hasNext());
	}
	
	public void testF() {
		s.add(e2);
		s.add(e1);
		NewApptBook s2 = newCollection();
		s2.add(e4);
		s.addAll(s2);
		assertEquals(3,s.size());
		Iterator<Appointment> it = s.iterator();
		assertSame(e1, it.next());
		assertSame(e2, it.next());
		assertSame(e4, it.next());
		assertException(NoSuchElementException.class, () -> it.next());
	}
	
	public void testG() {
		assertException(NoSuchElementException.class, () -> s.iterator().next());
		assertException(() -> s.iterator().remove(), IllegalStateException.class);
	}
	
	public void testH() {
		s.add(e3);
		s.add(e1);
		s.add(e2);
		Appointment e2a = new Appointment(new Period(now,Duration.DAY),"2: current");
		s.add(e2a);
		Iterator<Appointment> it = s.iterator();
		it.next();
		assertSame(e2, it.next());
		assertSame(e2a, it.next());
	}

	public void testI() {
		s.add(e1);
		assertFalse(s.remove(e2));
		assertTrue(s.remove(e1));
		assertEquals(0,s.size());	
		s.add(e2);
		assertSame(e2,s.iterator().next());
		assertEquals(1,s.size());
	}

	public void testJ() {
		s.add(e2);
		s.add(e3);
		Iterator<Appointment> it = s.iterator();
		it.next();
		it.remove();
		assertException(() -> it.remove(), IllegalStateException.class);
		assertEquals(1,s.size());
	}

	
	public void testK() {
		s.add(e1);
		s.add(e5);
		s.add(e2);
		Iterator<Appointment> it = s.iterator();
		it.next();
		it.remove();
		assertEquals(2,s.size());
		assertEquals(e2,it.next());
		it.remove();
		assertEquals(1,s.size());
		assertSame(e5,s.iterator().next());
		assertSame(e5,it.next());
	}

	public void testL() {
		s.add(e4);
		s.add(e4);
		s.add(e3);
		assertTrue(s.remove(e4));
		assertTrue(s.remove(e4));
		assertEquals(1,s.size());
	}
	
	public void testM() {
		s.add(e2);
		s.add(e3);
		s.add(e4);
		
		Iterator<Appointment> it1 = s.iterator();
		assertSame(e2,it1.next());
		
		Iterator<Appointment> it2 = s.iterator();
		assertSame(e2,it2.next());
		
		assertTrue(it1.hasNext());
		
		it2.remove();
		assertEquals(2,s.size());
		assertTrue(it2.hasNext());
		
		assertException(ConcurrentModificationException.class, () -> it1.hasNext());
		assertException(ConcurrentModificationException.class, () -> it1.next());
		assertException(() -> it1.remove(), ConcurrentModificationException.class);		
	}
 
	public void testN() {
		s.add(e1);
		s.add(e2);
		s.add(e3);
		s.add(e4);
		s.add(e5);
		assertSame(e1,s.iterator().next());
		s.add(e1);
		s.add(e2);
		s.add(e3);
		s.add(e4);
		s.add(e5);
		s.add(e1);
		assertEquals(11,s.size());
		assertTrue(s.remove(e1));
		assertEquals(10,s.size());
		
		Iterator<Appointment> it = s.iterator();
		assertSame(e1,it.next());
		assertSame(e1,it.next());
		assertSame(e2,it.next());
		assertSame(e2,it.next());
		assertSame(e3,it.next());
		assertSame(e3,it.next());
		assertSame(e4,it.next());
		assertSame(e4,it.next());
		assertSame(e5,it.next());
		assertSame(e5,it.next());
		assertFalse(it.hasNext());
	}
	
	public void testO() {
		s.add(e3);
		Iterator<Appointment> it = s.iterator(e2);
		assertEquals(e3, it.next());
		it = s.iterator(e3);
		assertEquals(e3, it.next());
		Iterator<Appointment> it1 = s.iterator(e4);
		assertException(NoSuchElementException.class, () -> it1.next());
	}
	
	public void testP() {
		s.add(e2);
		s.add(e3);
		s.add(e4);
		s.add(e3);
		Iterator<Appointment> it = s.iterator(e3);
		assertEquals(e3, it.next());
		assertEquals(e3, it.next());
		assertEquals(e4, it.next());
	}
	
	public void testQ() {
		s.add(e1);
		s.add(e2);
		s.add(e3);
		Iterator<Appointment> it = s.iterator(e2);
		assertException(() -> it.remove(), IllegalStateException.class);
	}
	
	public void testR() {
		assertException(NullPointerException.class, () -> s.iterator(null));
	}

	public void testS() {
		NewApptBook se = newCollection();
		se.add(e3);
		se.add(e4);
		se.add(e5);
		se.add(e3);
		se.add(e4);
		se.add(e5);
		se.add(e3);
		se.add(e4);
		se.add(e5);
		se.add(e3);
		se.add(e4);
		se.add(e5);
		se.add(e3);
		se.add(e4);
		se.add(e5);
		se.add(e3);
		se.add(e4);
		se.add(e5);
		se.add(e3);
		se.add(e4);
		se.add(e5);
		se.add(e3);
		se.add(e4);
		se.add(e5);
		// se has 24 elements
		s.add(e1);
		s.add(e2);
		s.addAll(se);
		assertEquals(26,s.size());
		s.addAll(se);
		assertEquals(50,s.size());
		se.addAll(s);
		assertEquals(74,se.size());
	}
	
	
	public void testT() {
		NewApptBook c = s.clone();
		assertEquals(0, c.size());
	}
	
	public void testU() {
		s.add(e1);
		NewApptBook c = s.clone();
		
		assertSame(e1,s.iterator().next());
		assertSame(e1,c.iterator().next());
	}
	
	public void testV() {
		s.add(e1);
		Iterator<Appointment> it = s.iterator();
		NewApptBook c = s.clone();
		
		c.add(e2);
		assertSame(e1,it.next());
		assertFalse(it.hasNext());
		it = c.iterator();
		it.next();
		assertSame(e2,it.next());
	}

	public void testW() {
		NewApptBook c = s.clone();
		
		s.add(e1);
		assertEquals(0,c.size());
		
		c = s.clone();
		assertEquals(1,c.size());
		assertSame(e1,c.iterator().next());
		
		c.add(e2);
		s.add(e3);
		assertTrue(c.remove(e1));
		assertSame(e2,c.iterator().next());
		
		Iterator<Appointment> it = s.iterator();
		assertSame(e1,it.next());
		
		it.remove();
		c.add(e4);
		assertTrue(c.remove(e2));
		
		assertSame(e3,it.next());
	}
	
}
