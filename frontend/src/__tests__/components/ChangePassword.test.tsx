/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ChangePassword from '../../components/ChangePassword';

// Mock the api module
jest.mock('../../utils/api', () => ({
  AUTH_API_BASE: 'http://localhost:12002',
  fetchWithAuthRetry: jest.fn(),
}));

import { fetchWithAuthRetry } from '../../utils/api';

const mockFetchWithAuthRetry = fetchWithAuthRetry as jest.MockedFunction<typeof fetchWithAuthRetry>;

describe('ChangePassword Component', () => {
  const mockOnClose = jest.fn();

  beforeEach(() => {
    mockFetchWithAuthRetry.mockReset();
    mockOnClose.mockClear();
  });

  it('should render change password form', () => {
    render(<ChangePassword onClose={mockOnClose} />);
    
    expect(screen.getByRole('heading', { name: 'Change Password' })).toBeInTheDocument();
    expect(screen.getByText('Update your account password')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter current password')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter new password')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Re-enter new password')).toBeInTheDocument();
  });

  it('should render submit and cancel buttons', () => {
    render(<ChangePassword onClose={mockOnClose} />);
    
    expect(screen.getByRole('button', { name: 'Change Password' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
  });

  it('should call onClose when cancel button is clicked', async () => {
    const user = userEvent.setup();
    render(<ChangePassword onClose={mockOnClose} />);
    
    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('should show error when passwords do not match', async () => {
    const user = userEvent.setup();
    render(<ChangePassword onClose={mockOnClose} />);
    
    await user.type(screen.getByPlaceholderText('Enter current password'), 'oldpassword');
    await user.type(screen.getByPlaceholderText('Enter new password'), 'newpassword');
    await user.type(screen.getByPlaceholderText('Re-enter new password'), 'different');
    await user.click(screen.getByRole('button', { name: 'Change Password' }));
    
    expect(screen.getByText('Passwords do not match.')).toBeInTheDocument();
    expect(mockFetchWithAuthRetry).not.toHaveBeenCalled();
  });

  it('should submit password change when passwords match', async () => {
    const user = userEvent.setup();
    mockFetchWithAuthRetry.mockResolvedValueOnce({
      ok: true,
      status: 200,
    } as Response);

    render(<ChangePassword onClose={mockOnClose} />);
    
    await user.type(screen.getByPlaceholderText('Enter current password'), 'oldpassword');
    await user.type(screen.getByPlaceholderText('Enter new password'), 'newpassword');
    await user.type(screen.getByPlaceholderText('Re-enter new password'), 'newpassword');
    await user.click(screen.getByRole('button', { name: 'Change Password' }));
    
    await waitFor(() => {
      expect(mockFetchWithAuthRetry).toHaveBeenCalledWith(
        expect.stringContaining('/api/auth/v1/password/change'),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            oldPassword: 'oldpassword',
            newPassword: 'newpassword',
          }),
        })
      );
    });
  });

  it('should show success message on successful password change', async () => {
    const user = userEvent.setup();
    mockFetchWithAuthRetry.mockResolvedValueOnce({
      ok: true,
      status: 200,
    } as Response);

    render(<ChangePassword onClose={mockOnClose} />);
    
    await user.type(screen.getByPlaceholderText('Enter current password'), 'oldpassword');
    await user.type(screen.getByPlaceholderText('Enter new password'), 'newpassword');
    await user.type(screen.getByPlaceholderText('Re-enter new password'), 'newpassword');
    await user.click(screen.getByRole('button', { name: 'Change Password' }));
    
    await waitFor(() => {
      expect(screen.getByText('Password changed successfully.')).toBeInTheDocument();
    });
  });

  it('should show error on password change failure', async () => {
    const user = userEvent.setup();
    mockFetchWithAuthRetry.mockResolvedValueOnce({
      ok: false,
      status: 400,
    } as Response);

    render(<ChangePassword onClose={mockOnClose} />);
    
    await user.type(screen.getByPlaceholderText('Enter current password'), 'oldpassword');
    await user.type(screen.getByPlaceholderText('Enter new password'), 'newpassword');
    await user.type(screen.getByPlaceholderText('Re-enter new password'), 'newpassword');
    await user.click(screen.getByRole('button', { name: 'Change Password' }));
    
    await waitFor(() => {
      expect(screen.getByText('Password change failed.')).toBeInTheDocument();
    });
  });

  it('should show network error on fetch failure', async () => {
    const user = userEvent.setup();
    mockFetchWithAuthRetry.mockRejectedValueOnce(new Error('Network error'));

    render(<ChangePassword onClose={mockOnClose} />);
    
    await user.type(screen.getByPlaceholderText('Enter current password'), 'oldpassword');
    await user.type(screen.getByPlaceholderText('Enter new password'), 'newpassword');
    await user.type(screen.getByPlaceholderText('Re-enter new password'), 'newpassword');
    await user.click(screen.getByRole('button', { name: 'Change Password' }));
    
    await waitFor(() => {
      expect(screen.getByText('Network error.')).toBeInTheDocument();
    });
  });

  it('should disable form fields while loading', async () => {
    const user = userEvent.setup();
    mockFetchWithAuthRetry.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(<ChangePassword onClose={mockOnClose} />);
    
    await user.type(screen.getByPlaceholderText('Enter current password'), 'oldpassword');
    await user.type(screen.getByPlaceholderText('Enter new password'), 'newpassword');
    await user.type(screen.getByPlaceholderText('Re-enter new password'), 'newpassword');
    await user.click(screen.getByRole('button', { name: 'Change Password' }));
    
    await waitFor(() => {
      expect(screen.getByPlaceholderText('Enter current password')).toBeDisabled();
      expect(screen.getByPlaceholderText('Enter new password')).toBeDisabled();
      expect(screen.getByPlaceholderText('Re-enter new password')).toBeDisabled();
      expect(screen.getByRole('button', { name: 'Changing...' })).toBeDisabled();
    });
  });

  it('should require all password fields', () => {
    render(<ChangePassword onClose={mockOnClose} />);
    
    expect(screen.getByPlaceholderText('Enter current password')).toBeRequired();
    expect(screen.getByPlaceholderText('Enter new password')).toBeRequired();
    expect(screen.getByPlaceholderText('Re-enter new password')).toBeRequired();
  });
});
