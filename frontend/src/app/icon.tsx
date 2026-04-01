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
      // Authentication icon: padlock
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'linear-gradient(135deg, #4F46E5, #7C3AED)',
          borderRadius: '6px',
        }}
      >
        {/* Lock shackle (U-shaped arch) */}
        <div
          style={{
            position: 'absolute',
            width: '14px',
            height: '9px',
            borderRadius: '7px 7px 0 0',
            border: '3px solid white',
            borderBottom: 'none',
            top: '6px',
            left: '9px',
          }}
        />
        {/* Lock body */}
        <div
          style={{
            position: 'absolute',
            width: '20px',
            height: '13px',
            background: 'white',
            borderRadius: '3px',
            top: '13px',
            left: '6px',
          }}
        />
        {/* Keyhole */}
        <div
          style={{
            position: 'absolute',
            width: '5px',
            height: '5px',
            borderRadius: '50%',
            background: '#5B21B6',
            top: '18px',
            left: '14px',
          }}
        />
      </div>
    ),
    {
      ...size,
    }
  )
}
