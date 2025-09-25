package es.uvigo.esei.xcs.service.util.security;

import java.util.function.Supplier;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.Stateless;

@Stateless(name = "owner-caller")
@RunAs("OWNER")
@PermitAll
public class OwnerRoleCaller implements RoleCaller {
	public <V> V call(Supplier<V> supplier) {
		return supplier.get();
	}
	
	public void run(Runnable run) {
		run.run();
	}
	
	public <V> V throwingCall(ThrowingSupplier<V> supplier) throws Throwable {
		return supplier.get();
	}
	
	public void throwingRun(ThrowingRunnable run) throws Throwable{
		run.run();
	}
}
