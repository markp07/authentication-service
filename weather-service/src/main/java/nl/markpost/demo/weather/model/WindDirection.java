package nl.markpost.demo.weather.model;

public enum WindDirection {
  N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW;

  public static WindDirection fromDegree(int degree) {
      if (degree >= 350 || degree <= 10) {
          return N;
      }
      if (degree >= 20 && degree <= 30) {
          return NNE;
      }
      if (degree >= 40 && degree <= 50) {
          return NE;
      }
      if (degree >= 60 && degree <= 70) {
          return ENE;
      }
      if (degree >= 80 && degree <= 100) {
          return E;
      }
      if (degree >= 110 && degree <= 120) {
          return ESE;
      }
      if (degree >= 130 && degree <= 140) {
          return SE;
      }
      if (degree >= 150 && degree <= 160) {
          return SSE;
      }
      if (degree >= 170 && degree <= 190) {
          return S;
      }
      if (degree >= 200 && degree <= 210) {
          return SSW;
      }
      if (degree >= 220 && degree <= 230) {
          return SW;
      }
      if (degree >= 240 && degree <= 250) {
          return WSW;
      }
      if (degree >= 260 && degree <= 280) {
          return W;
      }
      if (degree >= 290 && degree <= 300) {
          return WNW;
      }
      if (degree >= 310 && degree <= 320) {
          return NW;
      }
      if (degree >= 330 && degree <= 340) {
          return NNW;
      }
    return N; // fallback
  }
}

