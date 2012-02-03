public class Dummy {
	
	private String msg = null;
	
	public Dummy() {}
	
	public void setMessage(final String msg) {
		this.msg = msg;
	}
	
	public String getMessage() {
		return this.msg;
	}
	
	public static void main(String[] args) {
		final Dummy dummy = new Dummy();
		dummy.setMessage("Hello world!");
		System.out.println(dummy.getMessage());
	}
}