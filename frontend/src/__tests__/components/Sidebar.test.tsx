/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen } from '@testing-library/react';
import Sidebar from '../../components/Sidebar';

describe('Sidebar Component', () => {
  const mockOnNavigate = jest.fn();
  const mockOnLogout = jest.fn();

  beforeEach(() => {
    mockOnNavigate.mockClear();
    mockOnLogout.mockClear();
    // Set the environment variable for the version
    process.env.NEXT_PUBLIC_APP_VERSION = '1.6.4';
  });

  it('should display the correct version from environment variable', () => {
    render(
      <Sidebar
        username="testuser"
        activePage="dashboard"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );
    
    // Check that the version is displayed correctly
    expect(screen.getByText(/v1\.6\.4/)).toBeInTheDocument();
  });

  it('should display username in welcome message', () => {
    render(
      <Sidebar
        username="testuser"
        activePage="dashboard"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );
    
    expect(screen.getByText(/Welcome, testuser/)).toBeInTheDocument();
  });

  it('should display "User" when username is null', () => {
    render(
      <Sidebar
        username={null}
        activePage="dashboard"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );
    
    expect(screen.getByText(/Welcome, User/)).toBeInTheDocument();
  });

  it('should display fallback version when environment variable is not set', () => {
    // Temporarily unset the environment variable
    const originalVersion = process.env.NEXT_PUBLIC_APP_VERSION;
    delete process.env.NEXT_PUBLIC_APP_VERSION;

    render(
      <Sidebar
        username="testuser"
        activePage="dashboard"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );
    
    // Check that the fallback version is displayed
    expect(screen.getByText(/v0\.0\.0/)).toBeInTheDocument();

    // Restore the original value
    process.env.NEXT_PUBLIC_APP_VERSION = originalVersion;
  });
});
