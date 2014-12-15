public class CuentaKilometros {

	public static void printf(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}

	public static void main(String[] args) {
		CuentaKilometros ck = new CuentaKilometros();

		printf("hay que empezar por 0,0:" + ck );

		ck.addKilometros(0.01);

		printf("hay que seguir con 0,0:" + ck);

		for (int i = 0; i < 110; i++) {
			ck.addKilometros(0.01);
		}

		printf("hay que seguir con total 1,1" + ck);
		printf("hay que seguir con parcial 1,1:" + ck);

		ck.resetearParcial();

		printf("hay que seguir con total 1,1:" + ck);
		printf("hay que seguir con parcial 0,0:" + ck);
	}

	@Override
	public String toString() {
		return String.format("Total:%d,%d  Parcial:%d,%d",
				getTotalKilometros(), getTotalHectometros(),
				getParcialKilometros(), getParcialHectometros());
	}

	private double kilometrosTotales;
	private double kilometrosParciales;

	private void resetearParcial() {
		kilometrosParciales = 0;
	}

	private int getParcialHectometros() {
		return dameElPrimerDecimal(kilometrosParciales);
	}

	private int dameElPrimerDecimal(double d) {
		return ((int) (d * 10)) % 10;
	}

	private int getParcialKilometros() {
		return (int) Math.floor(kilometrosParciales);
	}

	private void addKilometros(double kilometros) {
		kilometrosParciales += kilometros;
		kilometrosTotales += kilometros;
	}

	private int getTotalHectometros() {
		return dameElPrimerDecimal(kilometrosTotales);
	}

	private int getTotalKilometros() {
		return (int) Math.floor(kilometrosTotales);
	}
}
