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
    delete process.env.NEXT_PUBLIC_EXTRA_MENU_ITEMS;
  });

  it('should display the correct version from environment variable', () => {
    render(
      <Sidebar
        username="testuser"
        activePage="profile"
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
        activePage="profile"
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
        activePage="profile"
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
        activePage="profile"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );
    
    // Check that the fallback version is displayed
    expect(screen.getByText(/v0\.0\.0/)).toBeInTheDocument();

    // Restore the original value
    process.env.NEXT_PUBLIC_APP_VERSION = originalVersion;
  });

  it('should render extra menu items from NEXT_PUBLIC_EXTRA_MENU_ITEMS', () => {
    process.env.NEXT_PUBLIC_EXTRA_MENU_ITEMS = JSON.stringify([
      { icon: 'IconHome', url: 'https://example.com', textNl: 'Thuis', textEn: 'Home' },
    ]);

    render(
      <Sidebar
        username="testuser"
        activePage="security"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );

    const link = screen.getByRole('link', { name: /Home/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute('href', 'https://example.com');
    expect(link).toHaveAttribute('target', '_blank');
    expect(link).toHaveAttribute('rel', 'noopener noreferrer');
  });

  it('should show English label for extra menu items when locale is en', () => {
    process.env.NEXT_PUBLIC_EXTRA_MENU_ITEMS = JSON.stringify([
      { icon: 'IconHome', url: 'https://example.com', textNl: 'Thuis', textEn: 'Home' },
    ]);

    render(
      <Sidebar
        username="testuser"
        activePage="security"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );

    // The mock returns locale 'en', so the English label should be shown
    expect(screen.getByRole('link', { name: /Home/i })).toBeInTheDocument();
    expect(screen.queryByText('Thuis')).not.toBeInTheDocument();
  });

  it('should not render extra menu items when env variable is empty', () => {
    process.env.NEXT_PUBLIC_EXTRA_MENU_ITEMS = '';

    render(
      <Sidebar
        username="testuser"
        activePage="profile"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );

    // No external links (extra menu items open in a new tab)
    const links = screen.queryAllByRole('link');
    expect(links.filter(l => l.getAttribute('target') === '_blank')).toHaveLength(0);
  });

  it('should not render extra menu items when env variable is invalid JSON', () => {
    process.env.NEXT_PUBLIC_EXTRA_MENU_ITEMS = 'not-valid-json';

    render(
      <Sidebar
        username="testuser"
        activePage="profile"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );

    // No external links (extra menu items open in a new tab)
    const links = screen.queryAllByRole('link');
    expect(links.filter(l => l.getAttribute('target') === '_blank')).toHaveLength(0);
  });

  it('should use fallback icon when icon name is unknown', () => {
    process.env.NEXT_PUBLIC_EXTRA_MENU_ITEMS = JSON.stringify([
      { icon: 'IconDoesNotExist', url: 'https://example.com', textNl: 'Test', textEn: 'Test' },
    ]);

    render(
      <Sidebar
        username="testuser"
        activePage="security"
        onNavigate={mockOnNavigate}
        onLogout={mockOnLogout}
      />
    );

    // Link should still be rendered with the fallback icon
    const link = screen.getByRole('link', { name: /Test/i });
    expect(link).toBeInTheDocument();
  });
});
