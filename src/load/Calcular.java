/**
 * 
 */
package load;

import meta.GRS80;

/**
 * 
 */
abstract class Calcular {

	/**
	 * 
	 */
	public Calcular(String medida) {
		this.medida = medida;
	}
	
	public String medida;
	
}

class CalcularGravedadNormal extends Calcular {
	public CalcularGravedadNormal(String medida, Double[] lats) {
		super(medida);
	}
	public Double[] calcularLakshmananLi(Double[] lats, Double[] h) {
		source = "https://library.seg.org/doi/10.1190/1.1487109";
		Double[] Betas = Beta(lats);
		Double[] zetasPrima = zetaPrima(Betas, h, lats);
		Double[] rsPrima = rPrima(Betas, h, lats);
		Double[] dsDoblePrima2 = dDoblePrima2(rsPrima, zetasPrima);
		Double[] rsDoblePrima2 = rDoblePrima2(rsPrima, zetasPrima);
		Double[] Ds = D(dsDoblePrima2);
		Double[] Rs = R(rsDoblePrima2);
		Double[] cosBsPrima = cosBPrima(Rs, Ds);
		Double[] bsPrima = bPrima(rsDoblePrima2, cosBsPrima);
		Double qZero = qZero();
		Double[] qsPrima = qPrima(bsPrima);
		Double[] Ws = W(bsPrima, BPrima(cosBsPrima));
		Double[] LakshmananLi1 = calcularLakshmananLi1(bsPrima, cosBsPrima);
		Double[] LakshmananLi2 = calcularLakshmananLi2(BPrima(cosBsPrima));
		Double[] LakshmananLi3 = calcularLakshmananLi3(qZero, qsPrima, bsPrima, BPrima(cosBsPrima));
		Double[] LakshmananLi4 = calcularLakshmananLi4(bsPrima);
		Double[] LakshmananLi = new Double[bsPrima.length];
		for (int i = 0; i < lats.length; i++) {
			LakshmananLi[i] = (1 / Ws[i]) * (LakshmananLi4[i] * LakshmananLi3[i] * LakshmananLi2[i] - LakshmananLi1[i]);
		}
		return LakshmananLi;
	}
	private Double[] calcularLakshmananLi4(Double[] bsPrima) {
		Double[] LakshmananLi4 = new Double[bsPrima.length];
		for (int i = 0; i < bsPrima.length; i++) {
			LakshmananLi4[i] = GRS80.GM / (Math.pow(bsPrima[i], 2) + Math.pow(GRS80.E, 2));
		}
		return LakshmananLi4;
	}
	private Double[] calcularLakshmananLi3(Double qZero, Double[] qsPrima, Double[] bsPrima, Double[] BPrima) {
		Double[] LakshmananLi3 = new Double[BPrima.length];
		for (int i = 0; i < BPrima.length; i++) {
			LakshmananLi3[i] = (Math.pow(GRS80.w, 2) * Math.pow(GRS80.a, 2) * GRS80.E * qsPrima[i]) / ((bsPrima[i] + Math.pow(GRS80.E, 2)) * qZero);
		}
		return LakshmananLi3;
	}
	private Double[] calcularLakshmananLi2(Double[] BPrima) {
		Double[] LakshmananLi2 = new Double[BPrima.length];
		for (int i = 0; i < BPrima.length; i++) {
			LakshmananLi2[i] = (1 / 2) * Math.pow(Math.sin(BPrima[i]), 2) - 1 / 6;
		}
		return LakshmananLi2;
	}
	private Double[] calcularLakshmananLi1(Double[] bPrima, Double[] cosBPrima) {
		Double[] LakshmananLi1 = new Double[cosBPrima.length];
		for (int i = 0; i < cosBPrima.length; i++) {
			LakshmananLi1[i] = Math.pow(GRS80.w, 2) * bPrima[i] * Math.pow(cosBPrima[i], 2);
		}
		return LakshmananLi1;
	}
	private Double[] qPrima(Double[] bPrima) {
		Double[] qsPrima = new Double[bPrima.length];
		Double[] qsPrima1= qPrima1(bPrima);
		Double[] qsPrima2= qPrima2(bPrima);
		for (int i = 0; i < bPrima.length; i++) {
			qsPrima[i] = 3 * (qsPrima2[i]) * (qsPrima1[i]) - 1;
		}
		return qsPrima;
	}
	private Double[] qPrima2(Double[] bPrima) {
		Double[] qsPrima2 = new Double[bPrima.length];
		for (int i = 0; i < bPrima.length; i++) {
			qsPrima2[i]= 1 + (Math.pow(bPrima[i], 2)/Math.pow(GRS80.E, 2));
		}
		return qsPrima2;
	}
	private Double[] qPrima1(Double[] bPrima) {
		Double[] qsPrima1 = new Double[bPrima.length];
		for (int i = 0; i < bPrima.length; i++) {
			qsPrima1[i]= 1 - (bPrima[i]/GRS80.E) * Math.atan(GRS80.E/bPrima[i]);
		}
		return qsPrima1;
	}
	private Double qZero() {
		Double qsZero1= ((3 * GRS80.b) / GRS80.E);
		Double qsZero2= Math.atan(GRS80.E / GRS80.b);
		Double qsZero3= (1 + ((3 * Math.pow(GRS80.b, 2)) / (Math.pow(GRS80.E, 2))));
		Double qsZero = (1 / 2) * (qsZero3 * qsZero2 - qsZero1);
		return qsZero;
	}
	private Double[] W(Double[] bPrima, Double[] BPrima) {
		Double[] Ws = new Double[bPrima.length];
		for (int i = 0; i < bPrima.length; i++) {
			Ws[i] = Math.sqrt((Math.pow(bPrima[i], 2) + Math.pow(GRS80.E, 2) * Math.pow(Math.sin(BPrima[i]), 2))/(Math.pow(bPrima[i], 2) + Math.pow(GRS80.E, 2)));
		}
		return Ws;
	}
	private Double[] bPrima(Double[] rDoblePrima2, Double[] cosBPrima) {
		Double[] bsPrima = new Double[rDoblePrima2.length];
		for (int i = 0; i < rDoblePrima2.length; i++) {
			bsPrima[i] = Math.sqrt(rDoblePrima2[i] - Math.pow(GRS80.E, 2) * Math.pow(cosBPrima[i], 2));
		}
		return bsPrima;
	}
	private Double[] BPrima(Double[] cosBPrima) {
		Double[] BsPrima = new Double[cosBPrima.length];;
		for (int i = 0; i < cosBPrima.length; i++) {
			BsPrima[i] = Math.acos(cosBPrima[i]);
		}
		return BsPrima;
	}
	private Double[] cosBPrima(Double[] R, Double[] D) {
		Double[] cosBsPrima1 = cosBPrima1(R, D);
		Double[] cosBsPrima2 = cosBPrima2(R, cosBsPrima1);
		return cosBsPrima2;
	}
	private Double[] cosBPrima2(Double[] R, Double[] cosBsPrima1) {
		Double[] cosBsPrima2 = new Double[R.length];
		for (int i = 0; i < R.length; i++) {
			cosBsPrima2[i] = Math.sqrt(1/2 + R[i]/2 - cosBsPrima1[i]);
		}
		return cosBsPrima2;
	}
	private Double[] cosBPrima1(Double[] R, Double[] D) {
		Double[] cosBsPrima1 = new Double[R.length];
		for (int i = 0; i < R.length; i++) {
			cosBsPrima1[i] = Math.sqrt(1/4 + Math.pow(R[i], 2) / 4 - D[i]/2);
		}
		return cosBsPrima1;
	}
	private Double[] R(Double[] rsDoblePrima2) {
		Double[] Rs = new Double[rsDoblePrima2.length];
		for (int i = 0; i < rsDoblePrima2.length; i++) {
			Rs[i] = rsDoblePrima2[i] / Math.pow(GRS80.E, 2);
		}
		return Rs;
	}
	private Double[] D(Double[] dsDoblePrima2) {
		Double[] Ds = new Double[dsDoblePrima2.length];
		for (int i = 0; i < dsDoblePrima2.length; i++) {
			Ds[i] = dsDoblePrima2[i] / Math.pow(GRS80.E, 2);
		}
		return Ds;
	}
	private Double[] rDoblePrima2(Double[] rsPrima, Double[] zetasPrima) {
		Double[] dsDoblePrima2 = new Double[rsPrima.length];
		for (int i = 0; i < rsPrima.length; i++) {
			dsDoblePrima2[i] = Math.pow(rsPrima[i], 2) + Math.pow(zetasPrima[i], 2);
		}
		return dsDoblePrima2;
	}
	private Double[] dDoblePrima2(Double[] rsPrima, Double[] zetasPrima) {
		Double[] dsDoblePrima2 = new Double[rsPrima.length];
		for (int i = 0; i < rsPrima.length; i++) {
			dsDoblePrima2[i] = Math.pow(rsPrima[i], 2) - Math.pow(zetasPrima[i], 2);
		}
		return dsDoblePrima2;
	}
	private Double[] rPrima(Double[] betas, Double[] h, Double[] lats) {
		Double[] rPrima = new Double[h.length];
		for (int i = 0; i < lats.length; i++) {
			rPrima[i] = GRS80.a * Math.cos(Math.toRadians(betas[i])) + h[i] * Math.cos(Math.toRadians(lats[i]));
		}
		return rPrima;
	}
	private Double[] zetaPrima(Double[] betas, Double[] h, Double[] lats) {
		Double[] zetaPrima = new Double[lats.length];
		for (int i = 0; i < lats.length; i++) {
			zetaPrima[i] = GRS80.b * Math.tan(Math.toRadians(betas[i])) + h[i] * Math.sin(Math.toRadians(lats[i]));
		}
		return zetaPrima;
	}
	private Double[] Beta(Double[] lats) {
		Double[] Betas = new Double[lats.length];
		for (int i = 0; i < lats.length; i++) {
			Betas[i] = (GRS80.b / GRS80.a) * Math.tan(Math.toRadians(lats[i]));
		}
		return Betas;
	}
	
	public static String source;
}
