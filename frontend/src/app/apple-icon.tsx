import { ImageResponse } from 'next/og'
 
// Image metadata
export const size = {
  width: 180,
  height: 180,
}
export const contentType = 'image/png'
 
// Image generation
export default function AppleIcon() {
  return new ImageResponse(
    (
      // Weather icon: sun with clouds
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'linear-gradient(to bottom, #87CEEB, #4A90E2)',
          borderRadius: '20%',
        }}
      >
        {/* Sun */}
        <div
          style={{
            width: '80px',
            height: '80px',
            borderRadius: '50%',
            background: '#FFD700',
            position: 'absolute',
            top: '30px',
            left: '40px',
            boxShadow: '0 0 20px rgba(255, 215, 0, 0.8)',
          }}
        />
        {/* Cloud */}
        <div
          style={{
            display: 'flex',
            position: 'absolute',
            bottom: '30px',
            right: '20px',
          }}
        >
          <div
            style={{
              width: '45px',
              height: '45px',
              borderRadius: '50%',
              background: 'white',
              position: 'absolute',
              left: '0px',
              bottom: '0px',
            }}
          />
          <div
            style={{
              width: '55px',
              height: '55px',
              borderRadius: '50%',
              background: 'white',
              position: 'absolute',
              left: '28px',
              bottom: '8px',
            }}
          />
          <div
            style={{
              width: '42px',
              height: '42px',
              borderRadius: '50%',
              background: 'white',
              position: 'absolute',
              left: '65px',
              bottom: '0px',
            }}
          />
        </div>
      </div>
    ),
    {
      ...size,
    }
  )
}
