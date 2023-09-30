import edu.uwm.cs351.Appointment;
import edu.uwm.cs351.Duration;
import edu.uwm.cs351.NewApptBook;
import edu.uwm.cs351.Period;
import edu.uwm.cs351.Time;

public class TestAppointmentCollection extends TestCollection<Appointment> {
	Time now = new Time();
	Appointment e0 = new Appointment(new Period(now,Duration.MINUTE), "0:blink");
	Appointment e1 = new Appointment(new Period(now,Duration.HOUR),"1: think");
	Appointment e2 = new Appointment(new Period(now,Duration.DAY),"2: current");
	Appointment e3 = new Appointment(new Period(now.add(Duration.HOUR),Duration.HOUR),"3: eat");
	Appointment e4 = new Appointment(new Period(now.add(Duration.HOUR.scale(2)),Duration.HOUR.scale(8)),"4: sleep");
	Appointment e5 = new Appointment(new Period(now.add(Duration.DAY),Duration.DAY),"5: tomorrow");
	Appointment e6 = new Appointment(new Period(now.add(Duration.DAY).add(Duration.HOUR),Duration.HOUR),"6: think tomorrow");
	Appointment e7 = new Appointment(new Period(now.add(Duration.DAY).add(Duration.HOUR.scale(1.5)),Duration.HOUR),"7: bathe tomorrow");
	Appointment e8 = new Appointment(new Period(now.add(Duration.DAY).add(Duration.HOUR.scale(2)),Duration.HOUR.scale(2)),"8: listen tomorrow");
	Appointment e9 = new Appointment(new Period(now.add(Duration.DAY).add(Duration.HOUR.scale(3)),Duration.HOUR.scale(8)),"9: sleeptomorrow");

	@Override
	protected void initCollections() {
		e = new Appointment[] { e0, e1, e2, e3, e4, e5, e6, e7, e8, e9 };
		c = new NewApptBook();
		permitNulls = false;
		preserveOrder = false;
	}

}
