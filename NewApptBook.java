package edu.uwm.cs351;

import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import junit.framework.TestCase;

/**
 * CHRISTIAN OROPEZA CS351 ...RECIEVED HELP FROM BOOK, LIBRARY TUTORS, ONLINE CS TUTOR, AND ADVICE FROM FRIENDS ON HOW TO APPROACH FIXING PERSISTENT BUGS.
 * JOHN KNIGHT, ESTELLE BRADY, JULLIAN MURANO,BIJAY PANTA (WHILE IN TUTORING LIBRARY SECTION) BUT NO CODE WAS SHARED.
 * for add method i had Julian walk through the concept of adding with 2 pre existing nodes and draw a picture like John Boyland had
 * me do to understand the data structure in office hours. then i let him fill in the blanks and hinted at possible issues with his 
 * logic until he understood the concept to move forward with implementation. No code or direct psuedocode logic was shared
 * I also had him do the same for the cursor explanation on the homework to which i also had him draw his own visual so he could
 * step through the checks and understand the logic behind "cursor:If remove can be called, the the cursor is the node that holds the element to be removed
 * in that eventuality. Otherwise, if remove cannot be called, it is the pointer to the next node
 * (if any)."...also helped Jiahui Yang understand the cursor concept by having him draw a picture 
 */

/**
 * A variant of the ApptBook ADT that follows the Collection model.
 * In particular, it has no sense of a current element.
 * All access to elements by the client must be through the iterator.
 * The {@link #add(Appointment)} method should add at the correct spot in sorted order in the collection.
 */
public class NewApptBook extends AbstractCollection<Appointment> implements Cloneable 
{	
	private static Consumer<String> reporter = (s) -> { System.err.println("Invariant error: " + s); };
	
	private boolean report(String error) 
	{
		reporter.accept(error);
		return false;
	}
	
	private Node head;
	private Node tail;

	private int version;
	private int manyItems;

	// TODO: Add all the contents here.
	// Remember:
	// - All public methods not marked @Override must be fully documented with javadoc
	// - A @Override method must be marked 'required', 'implementation', or 'efficiency'
	// - You need to define and check the data structure invariant
	//   (quite different from Homework #4)
	// - You should define a nested iterator class called MyIterator (with its own data structure), 
	//   and then the iterator() method simply returns a new instance.
	// You are permitted to copy in any useful code/comments from the Homework #3 or #4 solution.
	// But do not include any of the cursor-related methods, and in particular,
	// make sure you have no "cursor" field.
	
	private boolean wellFormed() 
	{
//		1. Similarly, the head cannot have any nodes before it. (But this should be checked at the
//		beginning so we can avoid getting stuck in a cycle.)
		if(head != null && head.prev != null)	return report("Head.Previous is NOT null!");
		
//		2. The prev and next links must match up: whenever one node’s next field points to another,
//		that one’s prev field must point back.
		{
			Node lag = null;
			for(Node p = head; p != null; lag = p, p = p.next)
			{
				if(lag != null)
				{
					if(lag.next != p || p.prev != lag)
					{
						return report("Head is not null BUT .next does not point to -> p and p.prev does not point to <- lag");
					}
				}
			}
		}

//		3. The tail pointer can be null if only if the head pointer is null.
		if((head == null && tail != null) || ( tail == null && head != null))
		{
			return report("Head is null but Tail is NOT!");
		}
		
//		4. If the tail pointer is not null, it must be reachable from the head pointer and have no nodes
//		after it.
		if(tail != null)
		{
			for(Node p = head; p != null; p = p.next)
			{
				if( p == tail.next)	return report("Tail is not null BUT Tail.Next is also NOT null");
				
				if(p != tail && p.next == null)	return report("Tail is not null BUT No node pointing to tail");
			}
		}
		
//		5. The declared number of items must be the same as the actual number of nodes in the list,
//		starting from the head.
		int count = 0;
		
		for(Node p = head; p != null; p = p.next)
		{
			++count;
		}
			
		if (manyItems != count)	return report("manyItems = " + manyItems + " but count = " + count);
		
//		6. None of the elements (“data” of the nodes) can be null.
//		7. The elements must be in non-decreasing order according to natural ordering.
		for(Node p = head; p != null; p = p.next)
		{
			if(p.data == null)	return report("Null data found");
			
			//	same as first for loop given to us to check if list is cyclic in previous HW
			for(Node i = head; i.next != null && i.next.data != null; i = i.next)
			{
				if(i.data.compareTo(i.next.data) > 0)	return report("data not natural order");
			}
		}
		return true;
	}
	
	/**
	 * Default constructor for NewApptBook class
	 */
	public NewApptBook() 
	{	
		manyItems = 0;
		version = 0;
		head =  null;
		tail = null;
		
		assert wellFormed() : "invariant failed at end of NewApptBook";
	}
	
	@Override	//	required
	public Iterator<Appointment> iterator() 
	{
		assert wellFormed() : "invariant failed at start of Iterator<Appointment> iterator";
		
		return new MyIterator();
	}
	
	/**
	 * Start an iterator before the first appointment that is equal or comes after the argument.
	 * @param start starting appointment, must not be null.
	 * @return an iterator starting at the given appointment.
	 */
	public Iterator<Appointment> iterator(Appointment a) 
	{
		assert wellFormed() : "invariant failed at start of Iterator<Appointment> iterator(Appointment a)";
		
		if(a == null) throw new NullPointerException();
		
		return new MyIterator(a);
	}
	
	/**
	 * Add a new element to this book, in order.  If an equal appointment is already
	 * in the book, it is inserted after the last of these. 
	 * If the new element would take this book beyond its current capacity,
	 * then the capacity is increased before adding the new element.
	 * The current element (if any) is not affected.
	 * @param element
	 *   the new element that is being added, must not be null
	 * @return 
	 * @postcondition
	 *   A new copy of the element has been added to this book. The current
	 *   element (whether or not is exists) is not changed.
	 * @exception IllegalArgumentException
	 *   indicates the parameter is null
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for increasing the book's capacity.
	 **/
	@Override	//	implementation
	public boolean add(Appointment a)
	{
		assert wellFormed() : "invariant failed at start of add";

		if (a == null) throw new NullPointerException("Cannot add Null!");
		
		if(manyItems >= Integer.MAX_VALUE) throw new OutOfMemoryError("insufficient memory for increasing the book's capacity");
		
		Node lag = null;
		Node p;
		
		for(p = tail; p != null; p = p.prev)
		{
			if(p.data.compareTo(a) <= 0)
			{
				break;
			}
			lag = p;
		}

		Node noddy = new Node(a);
		
		if(p == null)
		{
			head = noddy;
		}
		else
		{
			p.next = noddy;
		}
		
		if(lag == null )
		{
			tail = noddy;
		}
		else
		{
			lag.prev = noddy;
		}
		
		noddy.prev = p;
		noddy.next = lag;
		++manyItems;
		++version;
		
		assert wellFormed() : "invariant failed at end of add";

		return true;
	}
	
	/**
	 * Place all the appointments of another book (which may be the
	 * same book as this!) into this book in order as in {@link #insert}.
	 * The elements should added one by one from the start.
	 * The elements are probably not going to be placed in a single block.
	 * @param addend
	 *   a book whose contents will be placed into this book
	 * @precondition
	 *   The parameter, addend, is not null. 
	 * @postcondition
	 *   The elements from addend have been placed into
	 *   this book. The current element (if any) is
	 *   unchanged.
	 * @exception NullPointerException
	 *   Indicates that addend is null. 
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory to increase the size of this book.
	 **/
	public void addAll(NewApptBook addend)
	{
		assert wellFormed() : "invariant failed at start of addAll";
		
		if(addend == null) throw new NullPointerException("");
		
		if(manyItems >= Integer.MAX_VALUE) throw new OutOfMemoryError("");
		
		if(addend.manyItems == 0)	return;
		
		if(addend == this)	addend = addend.clone();
		
		for(Node p = addend.head; p != null; p = p.next)
		{
			add(p.data);
		}
		
		assert wellFormed() : "invariant failed at end of addAll";
		assert addend.wellFormed() : "invariant of addend broken in insertAll";
	}
	
	/**
	 * the state of the list when empty 
	 */
	@Override	//	efficiency
	public void clear()
	{
		assert wellFormed() : "invariant failed at start of clear";
		
		if(manyItems == 0)	return;
		head = null;
		tail = null;
		manyItems = 0;
		++version;
		
		assert wellFormed() : "invariant failed at end of clear";
	}
	
	/**
	 * Determine the number of elements in this book.
	 * @return the number of elements or manyItems
	 **/ 
	@Override	//	required
	public int size() 
	{
		assert wellFormed() : "invariant failed at start of size";
		
		return manyItems;
	}
	
	/**
	 * Generate a copy of this book.
	 * @return
	 *   The return value is a copy of this book. Subsequent changes to the
	 *   copy will not affect the original, nor vice versa.
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for creating the clone.
	 **/ 
	@Override	//	extend implementation
	public NewApptBook clone( ) 
	{ 
		assert wellFormed() : "invariant failed at start of clone";
		
		if(manyItems >= Integer.MAX_VALUE)	throw new OutOfMemoryError("Number larger than Integer Max Value");
		
		NewApptBook answer;
	
		try
		{
			answer = (NewApptBook) super.clone( );
		}
		catch (CloneNotSupportedException e)
		{  // This exception should not occur. But if it does, it would probably
			// indicate a programming error that made super.clone unavailable.
			// The most common error would be forgetting the "Implements Cloneable"
			// clause at the start of this class.
			throw new RuntimeException
			("This class does not implement Cloneable");
		}
		
		Node last = null;
		
		for(Node p = head; p != null; p = p.next)
		{
			Node noddy = new Node(p.data);
			
			if(p == tail) answer.tail = noddy;
			
			if(p == head) answer.head = noddy;
			
			if(last == null)
			{
				answer.head = noddy;
			}
			else
			{
				last.next = noddy;
			}
			last = noddy;
		}

		assert wellFormed() : "invariant failed at end of clone";
		assert answer.wellFormed() : "invariant on answer failed at end of clone";
		return answer;
	}
	
	/***************************************************************************************************
	 * 
	 * Node Class below
	 ***************************************************************************************************
	 */
	private static class Node
	{
		Appointment data;	//	similar to int data
		Node next;			//	similar to link Node
		Node prev;
		
		public Node (Appointment app) 
		{		
			data = app;
			
			next = null;
			
			prev = null;
		}
	}
	//***************************************************************************************************************************************************************
	
	private class MyIterator implements Iterator<Appointment> 
	{
		private Node cursor;
		private boolean canRemove = false;
		private int colVersion = version;
		
		/**
		 * @return boolean
		 */
		public boolean wellFormed() 
		{
			// TODO
			// - same first two tests as in Homework #3
			// - then check if the cursor is in the list
			// - it must be in list if we can remove the 
			// - current element or if it's not null
			
			if (!NewApptBook.this.wellFormed())	return false;
			
			// not my fault if invariant is broken
			if (!(version == colVersion))	return true;
			
			if (!(cursor == null) && head == null)	return report("Head is null but cursor is NOT!");
			
			if(!(canRemove == false) && cursor == null)	return report("Cursor is null but canRemove is True!");
			
			boolean precond = false;
			
			for(Node p = head; p != null; p = p.next)
			{
				if(p == cursor)	precond = true;
			}
			
			if(precond == false && cursor != null)	return report("precursor is NOT equal to a node in the list");
			
			return true;
		}

		public MyIterator() 
		{
			assert wellFormed() : "invariant failed at start of MyIterator";

			colVersion = version;
			cursor = head;
			canRemove = false;
		}
		
		public MyIterator(Appointment a) 
		{
			assert wellFormed() : "invariant failed at start of MyIterator(Appointment a)";
			
			if(a == null) throw new NullPointerException();
			
			colVersion = version;
			canRemove = false;
			
			for(Node p = head; p != null; p = p.next)
			{
				if(p.data.compareTo(a) >= 0)
				{
					cursor = p;
					break;
				}
			}
		}
		
		private void checkVersion() 
		{
			assert wellFormed() : "invariant failed at start of checkVersion";
			
			if (colVersion != version) throw new ConcurrentModificationException("stale iterator");
		}

		/**
		 * Return true if there are elements that have not yet been accessed by this iterator.
		 */
		@Override	//	required 
		public boolean hasNext() 
		{
			assert wellFormed() : "invariant failed at start of hasNext";
			checkVersion(); 

			if(canRemove == false && cursor == null)	return false;
			
			if(canRemove == false && cursor != null)	return true;
			
			if(canRemove == true && cursor.next == null)	return false;

			else	return true;
		}

		/*	The next method should, assuming a next element exists,
		 * point the iterator to the new current element and return that value.
		 * cursor = cursor.next;
		 * Advances the iterator, accessing and returning an element that had not yet been 
		 * accessed (now the current element). 
		 * If there is no such element (in which case hasNext() should have returned false), 
		 * then throw an instance of NoSuchElementException.
		 */
		@Override	//	required
		public Appointment next() 
		{
			assert wellFormed() : "invariant failed at start of next";
			if (!hasNext()) throw new NoSuchElementException("There is no next element");
			
			if(canRemove == true)	cursor = cursor.next;

			canRemove = true;
			
			assert wellFormed() : "invariant failed at end of next";
			
			return cursor.data;
		}
		
		/**
		 * Remove the current element (the one previously returned by next()).
		 * @postcondition
		 *   The current element has been removed from this book, and the 
		 *   following element (if there is one) is now the new current element. 
		 *   If there was no following element, then there is now no current 
		 *   element.
		 * @exception IllegalStateException
		 *   If next() has not yet been called, 
		 *   or if the last accessed element has already been removed, 
		 *   then this method throws an IllegalStateException, since there is no current element.
		 *   
		 *   The remove method will (of course) change the collection
		 *   version and thus should also change the iterator’s version, 
		 *   because it is still in synch with the (newly changed) collection.
		 **/
		@Override	//	implementation
		public void remove()
		{
			assert wellFormed() : "invariant failed at start of remove";
			checkVersion();
			if(canRemove == false) throw new IllegalStateException("canRemove is False!");
			
			if(head == cursor)	head = cursor.next;
			
			if(tail == cursor)	tail = cursor.prev;
			 
			if(cursor.prev != null)	cursor.prev.next = cursor.next;

			if(cursor.next != null)	cursor.next.prev = cursor.prev;
			
			canRemove = false;
			cursor = cursor.next;
			colVersion = ++version;
			--manyItems;
			
			assert wellFormed() : "invariant failed at end of remove";
		}
	}
	
//***************************************************************************************************************************************************************
	
	public static class TestInvariantChecker extends TestCase {
		Time now = new Time();
		Appointment e1 = new Appointment(new Period(now,Duration.HOUR),"1: think");
		Appointment e2 = new Appointment(new Period(now,Duration.DAY),"2: current");
		Appointment e3 = new Appointment(new Period(now.add(Duration.HOUR),Duration.HOUR),"3: eat");
		Appointment e4 = new Appointment(new Period(now.add(Duration.HOUR.scale(2)),Duration.HOUR.scale(8)),"4: sleep");
		Appointment e5 = new Appointment(new Period(now.add(Duration.DAY),Duration.DAY),"5: tomorrow");

		private int reports = 0;
		
		private void assertWellFormed(Object s, boolean expected) {
			reports = 0;
			Consumer<String> savedReporter = reporter;
			try {
				reporter = (String message) -> {
					++reports;
					if (message == null || message.trim().isEmpty()) {
						assertFalse("Uninformative report is not acceptable", true);
					}
					if (expected) {
						assertFalse("Reported error incorrectly: " + message, true);
					}
				};
				if (s instanceof NewApptBook) {
					assertEquals(expected, ((NewApptBook)s).wellFormed());
				} else {
					assertEquals(expected, ((NewApptBook.MyIterator)s).wellFormed());
				}
				if (!expected) {
					assertEquals("Expected exactly one invariant error to be reported", 1, reports);
				}
				reporter = null;
			} finally {
				reporter = savedReporter;
			}
		}
		
		protected Node newNode(Appointment a, Node p, Node n) {
			Node result = new Node(a);
			result.prev = p;
			result.next = n;
			result.data = a;
			return result;
		}
		
		protected Node newNode(Appointment a) {
			return newNode(a, null, null);
		}

		NewApptBook self;
		NewApptBook.MyIterator selfit;
		
		protected void setUp() {
			self = new NewApptBook();
			self.head = self.tail = null;
			self.manyItems = 0;
			self.version = 17;
			selfit = self.new MyIterator();
			selfit.canRemove = false;
			selfit.cursor = null;
			selfit.colVersion = 17;
		}

		public void testA0() {
			assertWellFormed(self, true);
		}
		
		public void testA1() {
			self.tail = new Node(e1);
			assertWellFormed(self, false);
		}
		
		public void testA2() {
			self.manyItems = -1;
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, false);
		}
		
		public void testA3() {
			self.head = self.tail = newNode(null);
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, false);
		}
		
		public void testB0() {
			self.head = self.tail = newNode(e2);
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, true);
		}
		
		public void testB1() {
			self.head = newNode(e3);
			self.tail = newNode(e3);
			self.manyItems = 1;
			assertWellFormed(self, false);
			self.manyItems = 2;
			assertWellFormed(self, false);
		}
		
		public void testB2() {
			self.head = newNode(e3);
			self.tail = null;
			self.manyItems = 1;
			assertWellFormed(self, false);
			self.manyItems = 0;
			assertWellFormed(self, false);
		}
		
		public void testB3() {
			self.head = self.tail = newNode(e2);
			self.head.prev = newNode(e1,null,self.head);
			self.tail.next = newNode(e3,self.tail,null);
			self.manyItems = 0;
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, false);
			self.manyItems = 2;
			assertWellFormed(self, false);
			self.manyItems = 3;
			assertWellFormed(self, false);			
		}
		
		public void testB4() {
			self.head = self.tail = newNode(e1);
			self.head.prev = self.head;
			self.tail.next = self.tail;
			self.manyItems = 0;
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, false);
			self.manyItems = 2;
			assertWellFormed(self, false);
			self.manyItems = 3;
			assertWellFormed(self, false);
		}
		
		public void testC0() {
			self.head = newNode(e4);
			self.tail = newNode(e5);
			self.manyItems = 2;
			assertWellFormed(self, false);
			self.head.next = self.tail;
			assertWellFormed(self, false);
			self.tail.prev = self.head;
			assertWellFormed(self, true);
		}
		
		public void testC1() {
			self.head = newNode(e2);
			self.tail = newNode(e1);
			self.head.next = self.tail;
			self.tail.prev = self.head;
			self.manyItems = 2;
			assertWellFormed(self, false);
		}
		
		public void testC2() {
			self.head = newNode(e3);
			self.tail = newNode(e3);
			self.head.prev = self.head.next = self.tail;
			self.tail.prev = self.tail.next = self.head;
			self.manyItems = 2;
			assertWellFormed(self, false);			
			self.manyItems = 3;
			assertWellFormed(self, false);			
			self.manyItems = Integer.MAX_VALUE;
			assertWellFormed(self, false);			
		}
		
		public void testC3() {
			self.head = newNode(e3);
			self.tail = newNode(e3);
			self.manyItems = 2;
			self.head.next = self.tail;
			self.tail.prev = self.head;
			assertWellFormed(self, true);
			self.tail.next = newNode(e3,self.tail,null);
			assertWellFormed(self, false);
			self.tail.next = null;
			self.tail = null;
			assertWellFormed(self, false);
			self.tail = new Node(e3);
			self.tail.prev = self.head;
			assertWellFormed(self, false);
			self.tail.next = self.tail;
			self.tail.prev = self.tail;
			assertWellFormed(self, false);
		}
		
		public void testD0() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			self.head = n1;
			self.tail = n3;
			self.manyItems = 3;
			assertWellFormed(self, true);
		}
		
		public void testD1() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; // n3.prev = n2;	
			self.head = n1;
			self.tail = n3;
			self.manyItems = 3;
			assertWellFormed(self, false);
			
			n3.prev = n2;
			n2.prev = null;
			assertWellFormed(self, false);
			
			n2.prev = newNode(e1,null,n2);
			assertWellFormed(self, false);
			
			n2.prev = n1;
			n3.prev = newNode(e2,n1,n3);
			assertWellFormed(self, false);
		}
		
		public void testD2() {
			Node n1 = newNode(e4);
			Node n2 = newNode(e4);
			Node n3 = newNode(e4);
			Node n4 = newNode(e4);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			self.manyItems = 4;
			self.head = n1;
			self.tail = n4;
			assertWellFormed(self, true);
			
			self.tail = n3;
			assertWellFormed(self, false);
			self.manyItems = 3;
			assertWellFormed(self, false);
			
			self.tail = n4;
			self.head = n2;
			assertWellFormed(self, false);
		}
		
		public void testD3() {
			Node n1 = newNode(e4);
			Node n2 = newNode(e4);
			Node n3 = newNode(e4);
			Node n4 = newNode(e4);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			self.manyItems = 4;
			self.head = n1;
			self.tail = n4;
			assertWellFormed(self, true);
			
			n1.data = null;
			assertWellFormed(self, false);
			n1.data = e4;
			
			n2.data = null;
			assertWellFormed(self, false);
			n2.data = e4;
			
			n3.data = null;
			assertWellFormed(self, false);
			n3.data = e4;
			
			n4.data = null;
			assertWellFormed(self, false);
			n4.data = e4;
			
			assertWellFormed(self, true);
		}
		
		public void testE0() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			Node n4 = newNode(e4);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			assertWellFormed(self, true);
			
			n1.prev = newNode(null,null,n1);
			assertWellFormed(self, false);
			n1.prev = null;
			
			n2.prev = newNode(e1,null,n2);
			assertWellFormed(self, false);
			n2.prev = null;
			assertWellFormed(self, false);
			n2.prev = n1;
			
			n3.prev = newNode(e2,null,n3);
			assertWellFormed(self, false);
			n3.prev = null;
			assertWellFormed(self, false);
			n3.prev = n2;
			
			n4.prev = newNode(e3,null,n4);
			assertWellFormed(self, false);
			n4.prev = null;
			assertWellFormed(self, false);
			n4.prev = n3;
			
			n5.prev = newNode(e4,null,n5);
			assertWellFormed(self, false);
			n5.prev = null;
			assertWellFormed(self, false);
			n5.prev = n4;
			
			assertWellFormed(self, true);
		}
		
		public void testE1() {
			Node n1 = newNode(e5);
			Node n2 = newNode(e5);
			Node n3 = newNode(e5);
			Node n4 = newNode(e5);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			assertWellFormed(self, true);
			
			n1.next = n1;
			assertWellFormed(self, false);
			n1.next = n2;
			
			n2.next = n1;
			assertWellFormed(self, false);
			n2.next = n2;
			assertWellFormed(self, false);
			n2.next = n3;
			
			n3.next = n1;
			assertWellFormed(self, false);
			n3.next = n2;
			assertWellFormed(self, false);
			n3.next = n3;
			assertWellFormed(self, false);
			n3.next = n4;
			
			n4.next = n1;
			assertWellFormed(self, false);
			n4.next = n2;
			assertWellFormed(self, false);
			n4.next = n3;
			assertWellFormed(self, false);
			n4.next = n4;
			assertWellFormed(self, false);
			n4.next = n5;
			
			n5.next = n1;
			assertWellFormed(self, false);
			n5.next = n2;
			assertWellFormed(self, false);
			n5.next = n3;
			assertWellFormed(self, false);
			n5.next = n4;
			assertWellFormed(self, false);
			n5.next = n5;
			assertWellFormed(self, false);
			n5.next = null;
			
			assertWellFormed(self, true);
		}
		
		public void testE2() {
			Node n1 = newNode(e5);
			Node n2 = newNode(e5);
			Node n3 = newNode(e5);
			Node n4 = newNode(e5);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			assertWellFormed(self, true);

			n1.prev = n1;
			assertWellFormed(self, false);
			n1.prev = n2;
			assertWellFormed(self, false);
			n1.prev = n3;
			assertWellFormed(self, false);
			n1.prev = n4;
			assertWellFormed(self, false);
			n1.prev = n5;
			assertWellFormed(self, false);
			n1.prev = null;
			
			n2.prev = null;
			assertWellFormed(self, false);
			n2.prev = n2;
			assertWellFormed(self, false);
			n2.prev = n3;
			assertWellFormed(self, false);
			n2.prev = n4;
			assertWellFormed(self, false);
			n2.prev = n5;
			assertWellFormed(self, false);
			n2.prev = n1;
			
			n3.prev = null;
			assertWellFormed(self, false);
			n3.prev = n1;
			assertWellFormed(self, false);
			n3.prev = n3;
			assertWellFormed(self, false);
			n3.prev = n4;
			assertWellFormed(self, false);
			n3.prev = n5;
			assertWellFormed(self, false);
			n3.prev = n2;
			
			n4.prev = null;
			assertWellFormed(self, false);
			n4.prev = n1;
			assertWellFormed(self, false);
			n4.prev = n2;
			assertWellFormed(self, false);
			n4.prev = n4;
			assertWellFormed(self, false);
			n4.prev = n5;
			assertWellFormed(self, false);
			n4.prev = n3;
			
			n5.prev = null;
			assertWellFormed(self, false);
			n5.prev = n1;
			assertWellFormed(self, false);
			n5.prev = n2;
			assertWellFormed(self, false);
			n5.prev = n3;
			assertWellFormed(self, false);
			n5.prev = n5;
			assertWellFormed(self, false);
			n5.prev = n4;
			
			assertWellFormed(self, true);
		}
		
		public void testE3() {
			Node n1 = newNode(e5);
			Node n2 = newNode(e5);
			Node n3 = newNode(e5);
			Node n4 = newNode(e5);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;

			Node m1 = newNode(e5);
			Node m2 = newNode(e5);
			Node m3 = newNode(e5);
			Node m4 = newNode(e5);
			Node m5 = newNode(e5);
			m1.next = m2; m2.prev = m1;
			m2.next = m3; m3.prev = m2;
			m3.next = m4; m4.prev = m3;
			m4.next = m5; m5.prev = m4;
			
			self.manyItems = 5;
			self.head = n1;
			self.tail = m5;
			assertWellFormed(self, false);

			m2.prev = n1;
			assertWellFormed(self, false);
			m3.prev = n2;
			assertWellFormed(self, false);
			m4.prev = n3;
			assertWellFormed(self, false);
			m5.prev = n4;
			assertWellFormed(self, false);
			
			n4.next = m5;
			assertWellFormed(self, true);			
		}

		public void testE4() {
			Node n1 = newNode(e5);
			Node n2 = newNode(e5);
			Node n3 = newNode(e5);
			Node n4 = newNode(e5);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			
			assertWellFormed(self, true);
			
			n3.next = n1;
			n1.prev = n3;
			assertWellFormed(self, false);
			
			Node m1 = newNode(e5);
			Node m2 = newNode(e5);
			Node m3 = newNode(e5);
			Node m4 = newNode(e5);
			Node m5 = newNode(e5);
			m1.next = m2; m2.prev = m1;
			m2.next = m3; m3.prev = m2;
			m3.next = m4; m4.prev = m3;
			m4.next = m5; m5.prev = m4;
			
			n3.next = n4;
			n1.prev = null;
			assertWellFormed(self, true);
			
			n5.next = n1;
			n1.prev = n5;
			self.tail = m5;
			assertWellFormed(self, false);
		}
		
		public void testI0() {
			selfit.canRemove = false;
			assertWellFormed(selfit, true);
		}
		
		public void testI1() {
			selfit.cursor = newNode(e1);
			assertWellFormed(selfit, false);
			selfit.colVersion = 16;
			assertWellFormed(selfit, true);
			self.head = selfit.cursor;
			assertWellFormed(selfit, false);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
			selfit.colVersion = 17;
			assertWellFormed(selfit, false);
		}
		
		public void testI2() {
			selfit.canRemove = true;
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e2);
			assertWellFormed(selfit, false);
			selfit.colVersion = 0;
			assertWellFormed(selfit, true);
		}
		
		public void testI3() {
			self.head = self.tail = newNode(e3);
			self.manyItems = 1;
			assertWellFormed(self, true);
			
			selfit.canRemove = false;
			assertWellFormed(selfit, true);
			selfit.cursor = self.head;
			assertWellFormed(selfit, true);
			selfit.canRemove = true;
			assertWellFormed(selfit, true);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
		}
		
		public void testI4() {
			self.head = newNode(e1);
			self.tail = newNode(e2);
			self.head.next = self.tail;
			self.tail.prev = self.head;
			self.manyItems = 2;
			assertWellFormed(self, true);
			
			selfit.canRemove = false;
			selfit.cursor = null;
			assertWellFormed(selfit, true);
			selfit.cursor = self.head;
			assertWellFormed(selfit, true);
			selfit.cursor = self.tail;
			assertWellFormed(selfit, true);
			
			selfit.canRemove = true;
			assertWellFormed(selfit, true);
			selfit.cursor = self.head;
			assertWellFormed(selfit, true);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
		}
		
		public void testI5() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			self.head = n1;
			self.tail = n3;
			self.manyItems = 3;
			assertWellFormed(self, true);
			
			selfit.canRemove = false;
			
			selfit.cursor = newNode(e1,null,n2);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e2,n1,n3);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e3,n2,null);
			assertWellFormed(selfit, false);
			selfit.cursor = null;
			
			assertWellFormed(selfit, true);
		}
		
		public void testI6() {
			Node n1 = newNode(e4);
			Node n2 = newNode(e4);
			Node n3 = newNode(e4);
			Node n4 = newNode(e4);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			self.manyItems = 4;
			self.head = n1;
			self.tail = n4;
			assertWellFormed(self, true);

			selfit.canRemove = true;
			
			selfit.cursor = newNode(e1,null,n2);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e2,n1,n3);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e3,n2,n4);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e4,n3,null);
			assertWellFormed(selfit, false);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
		}
		
		public void testI7() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			Node n4 = newNode(e4);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			assertWellFormed(self, true);

			selfit.colVersion = 14;
			selfit.canRemove = true;
			
			selfit.cursor = newNode(e1,null,n2);
			assertWellFormed(selfit, true);
			selfit.cursor = newNode(e2,n1,n3);
			assertWellFormed(selfit, true);
			selfit.cursor = newNode(e3,n2,n4);
			assertWellFormed(selfit, true);
			selfit.cursor = newNode(e4,n3,n4);
			assertWellFormed(selfit, true);
			selfit.cursor = newNode(e5,n4,null);
			assertWellFormed(selfit, true);
			
			selfit.colVersion = self.version;
			assertWellFormed(selfit, false);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
			selfit.canRemove = false;
			assertWellFormed(selfit, true);
		}
	}
}
