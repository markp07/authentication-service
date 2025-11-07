import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Weather App",
  description: "A modern weather app with authentication and user profile.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="bg-gradient-to-br from-blue-100 via-white to-blue-300 dark:from-gray-900 dark:via-gray-800 dark:to-gray-700 min-h-screen">
      <body
        className="antialiased min-h-screen flex flex-col items-center justify-center text-gray-900 dark:text-gray-100 font-sans"
      >
        <div className="w-full max-w-full mx-auto flex flex-col min-h-screen">
          {children}
        </div>
      </body>
    </html>
  );
}
