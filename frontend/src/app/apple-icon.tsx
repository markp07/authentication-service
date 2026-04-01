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
      // Authentication icon: padlock
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'linear-gradient(135deg, #4F46E5, #7C3AED)',
          borderRadius: '20%',
        }}
      >
        {/* Lock shackle (U-shaped arch) */}
        <div
          style={{
            position: 'absolute',
            width: '79px',
            height: '51px',
            borderRadius: '39px 39px 0 0',
            border: '17px solid white',
            borderBottom: 'none',
            top: '34px',
            left: '51px',
          }}
        />
        {/* Lock body */}
        <div
          style={{
            position: 'absolute',
            width: '113px',
            height: '73px',
            background: 'white',
            borderRadius: '17px',
            top: '73px',
            left: '34px',
          }}
        />
        {/* Keyhole */}
        <div
          style={{
            position: 'absolute',
            width: '28px',
            height: '28px',
            borderRadius: '50%',
            background: '#5B21B6',
            top: '101px',
            left: '76px',
          }}
        />
      </div>
    ),
    {
      ...size,
    }
  )
}
