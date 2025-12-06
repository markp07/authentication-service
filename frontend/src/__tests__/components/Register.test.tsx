/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Register from '../../components/Register';

// Mock fetch
const mockFetch = jest.fn();
global.fetch = mockFetch;

describe('Register Component', () => {
  const mockOnSuccess = jest.fn();
  const mockOnLogin = jest.fn();

  beforeEach(() => {
    mockFetch.mockReset();
    mockOnSuccess.mockClear();
    mockOnLogin.mockClear();
  });

  it('should render registration form', () => {
    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    expect(screen.getByRole('heading', { name: 'Register' })).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Confirm Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Register' })).toBeInTheDocument();
  });

  it('should render back to login button', () => {
    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    const backButton = screen.getByRole('button', { name: /back to login/i });
    expect(backButton).toBeInTheDocument();
  });

  it('should call onLogin when back button is clicked', async () => {
    const user = userEvent.setup();
    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    await user.click(screen.getByRole('button', { name: /back to login/i }));
    
    expect(mockOnLogin).toHaveBeenCalledTimes(1);
  });

  it('should show error when passwords do not match', async () => {
    const user = userEvent.setup();
    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    await user.type(screen.getByPlaceholderText('Email'), 'test@example.com');
    await user.type(screen.getByPlaceholderText('Username'), 'testuser');
    await user.type(screen.getByPlaceholderText('Password'), 'password123');
    await user.type(screen.getByPlaceholderText('Confirm Password'), 'different');
    await user.click(screen.getByRole('button', { name: 'Register' }));
    
    expect(screen.getByText('Passwords do not match.')).toBeInTheDocument();
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it('should submit registration when form is valid', async () => {
    const user = userEvent.setup();
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ message: 'Success' }),
    });

    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    await user.type(screen.getByPlaceholderText('Email'), 'test@example.com');
    await user.type(screen.getByPlaceholderText('Username'), 'testuser');
    await user.type(screen.getByPlaceholderText('Password'), 'password123');
    await user.type(screen.getByPlaceholderText('Confirm Password'), 'password123');
    await user.click(screen.getByRole('button', { name: 'Register' }));
    
    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/auth/v1/register'),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            email: 'test@example.com',
            userName: 'testuser',
            password: 'password123',
          }),
        })
      );
    });
  });

  it('should show success message on successful registration', async () => {
    const user = userEvent.setup();
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 201,
    });

    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    await user.type(screen.getByPlaceholderText('Email'), 'test@example.com');
    await user.type(screen.getByPlaceholderText('Username'), 'testuser');
    await user.type(screen.getByPlaceholderText('Password'), 'password123');
    await user.type(screen.getByPlaceholderText('Confirm Password'), 'password123');
    await user.click(screen.getByRole('button', { name: 'Register' }));
    
    await waitFor(() => {
      expect(screen.getByText(/registration successful/i)).toBeInTheDocument();
    });
  });

  it('should show error on registration failure', async () => {
    const user = userEvent.setup();
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 400,
    });

    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    await user.type(screen.getByPlaceholderText('Email'), 'test@example.com');
    await user.type(screen.getByPlaceholderText('Username'), 'testuser');
    await user.type(screen.getByPlaceholderText('Password'), 'password123');
    await user.type(screen.getByPlaceholderText('Confirm Password'), 'password123');
    await user.click(screen.getByRole('button', { name: 'Register' }));
    
    await waitFor(() => {
      expect(screen.getByText('Registration failed. Try again.')).toBeInTheDocument();
    });
  });

  it('should show network error on fetch failure', async () => {
    const user = userEvent.setup();
    mockFetch.mockRejectedValueOnce(new Error('Network error'));

    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    await user.type(screen.getByPlaceholderText('Email'), 'test@example.com');
    await user.type(screen.getByPlaceholderText('Username'), 'testuser');
    await user.type(screen.getByPlaceholderText('Password'), 'password123');
    await user.type(screen.getByPlaceholderText('Confirm Password'), 'password123');
    await user.click(screen.getByRole('button', { name: 'Register' }));
    
    await waitFor(() => {
      expect(screen.getByText('Network error.')).toBeInTheDocument();
    });
  });

  it('should disable submit button while loading', async () => {
    const user = userEvent.setup();
    mockFetch.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    await user.type(screen.getByPlaceholderText('Email'), 'test@example.com');
    await user.type(screen.getByPlaceholderText('Username'), 'testuser');
    await user.type(screen.getByPlaceholderText('Password'), 'password123');
    await user.type(screen.getByPlaceholderText('Confirm Password'), 'password123');
    await user.click(screen.getByRole('button', { name: 'Register' }));
    
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Registering...' })).toBeDisabled();
    });
  });

  it('should require all fields', () => {
    render(<Register onSuccess={mockOnSuccess} onLogin={mockOnLogin} />);
    
    expect(screen.getByPlaceholderText('Email')).toBeRequired();
    expect(screen.getByPlaceholderText('Username')).toBeRequired();
    expect(screen.getByPlaceholderText('Password')).toBeRequired();
    expect(screen.getByPlaceholderText('Confirm Password')).toBeRequired();
  });
});
