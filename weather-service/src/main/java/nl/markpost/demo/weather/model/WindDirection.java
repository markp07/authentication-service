package nl.markpost.demo.weather.model;

      public enum WindDirection {
        N, NE, E, SE, S, SW, W, NW;

        public static WindDirection fromDegree(int degree) {
          // Normalize degree to 0-359
          degree = ((degree % 360) + 360) % 360;

          if (degree >= 338 || degree < 23) {
            return N;
          }
          if (degree >= 23 && degree < 68) {
            return NE;
          }
          if (degree >= 68 && degree < 113) {
            return E;
          }
          if (degree >= 113 && degree < 158) {
            return SE;
          }
          if (degree >= 158 && degree < 203) {
            return S;
          }
          if (degree >= 203 && degree < 248) {
            return SW;
          }
          if (degree >= 248 && degree < 293) {
            return W;
          }
          if (degree >= 293 && degree < 338) {
            return NW;
          }
          return N; // fallback
        }
      }