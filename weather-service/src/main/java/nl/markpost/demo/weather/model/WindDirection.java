package nl.markpost.demo.weather.model;

public enum WindDirection {
    N, N_NE, NE, E_NE, E, E_SE, SE, S_SE, S, S_SW, SW, W_SW, W, W_NW, NW, N_NW;

    public static WindDirection fromDegree(int degree) {
        if (degree >= 350 || degree == 360 || degree <= 10) return N;
        if (degree >= 20 && degree <= 30) return N_NE;
        if (degree >= 40 && degree <= 50) return NE;
        if (degree >= 60 && degree <= 70) return E_NE;
        if (degree >= 80 && degree <= 100) return E;
        if (degree >= 110 && degree <= 120) return E_SE;
        if (degree >= 130 && degree <= 140) return SE;
        if (degree >= 150 && degree <= 160) return S_SE;
        if (degree >= 170 && degree <= 190) return S;
        if (degree >= 200 && degree <= 210) return S_SW;
        if (degree >= 220 && degree <= 230) return SW;
        if (degree >= 240 && degree <= 250) return W_SW;
        if (degree >= 260 && degree <= 280) return W;
        if (degree >= 290 && degree <= 300) return W_NW;
        if (degree >= 310 && degree <= 320) return NW;
        if (degree >= 330 && degree <= 340) return N_NW;
        return N; // fallback
    }
}

