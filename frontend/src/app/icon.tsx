import { ImageResponse } from 'next/og'
 
// Image metadata
export const size = {
  width: 32,
  height: 32,
}
export const contentType = 'image/png'
 
// Image generation
export default function Icon() {
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
          borderRadius: '6px',
        }}
      >
        {/* Sun */}
        <div
          style={{
            width: '18px',
            height: '18px',
            borderRadius: '50%',
            background: '#FFD700',
            position: 'absolute',
            top: '8px',
            left: '10px',
            boxShadow: '0 0 8px rgba(255, 215, 0, 0.6)',
          }}
        />
        {/* Cloud */}
        <div
          style={{
            display: 'flex',
            position: 'absolute',
            bottom: '6px',
            right: '4px',
          }}
        >
          <div
            style={{
              width: '10px',
              height: '10px',
              borderRadius: '50%',
              background: 'white',
              position: 'absolute',
              left: '0px',
              bottom: '0px',
            }}
          />
          <div
            style={{
              width: '12px',
              height: '12px',
              borderRadius: '50%',
              background: 'white',
              position: 'absolute',
              left: '6px',
              bottom: '2px',
            }}
          />
          <div
            style={{
              width: '9px',
              height: '9px',
              borderRadius: '50%',
              background: 'white',
              position: 'absolute',
              left: '14px',
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
