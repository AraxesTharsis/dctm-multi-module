public class App {
	
	private String msg = null;
	
	public App() {}
	
	public void setMessage(final String msg) {
		this.msg = msg;
	}
	
	public String getMessage() {
		return this.msg;
	}
	
	public static void main(String[] args) {
		final App app = new App();
		app.setMessage("Hello world!");
		System.out.println(app.getMessage());
	}
}