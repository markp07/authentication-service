/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import Modal from '../../components/Modal';

describe('Modal Component', () => {
  const mockOnClose = jest.fn();

  beforeEach(() => {
    mockOnClose.mockClear();
    // Reset body overflow style
    document.body.style.overflow = 'unset';
  });

  it('should not render when open is false', () => {
    render(
      <Modal open={false} onClose={mockOnClose}>
        <div>Modal Content</div>
      </Modal>
    );
    
    expect(screen.queryByText('Modal Content')).not.toBeInTheDocument();
  });

  it('should render children when open is true', () => {
    render(
      <Modal open={true} onClose={mockOnClose}>
        <div>Modal Content</div>
      </Modal>
    );
    
    expect(screen.getByText('Modal Content')).toBeInTheDocument();
  });

  it('should render close button', () => {
    render(
      <Modal open={true} onClose={mockOnClose}>
        <div>Modal Content</div>
      </Modal>
    );
    
    expect(screen.getByRole('button', { name: /close modal/i })).toBeInTheDocument();
  });

  it('should call onClose when close button is clicked', () => {
    render(
      <Modal open={true} onClose={mockOnClose}>
        <div>Modal Content</div>
      </Modal>
    );
    
    fireEvent.click(screen.getByRole('button', { name: /close modal/i }));
    
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('should set body overflow to hidden when modal opens', () => {
    render(
      <Modal open={true} onClose={mockOnClose}>
        <div>Modal Content</div>
      </Modal>
    );
    
    expect(document.body.style.overflow).toBe('hidden');
  });

  it('should reset body overflow when modal closes', () => {
    const { rerender } = render(
      <Modal open={true} onClose={mockOnClose}>
        <div>Modal Content</div>
      </Modal>
    );
    
    expect(document.body.style.overflow).toBe('hidden');
    
    rerender(
      <Modal open={false} onClose={mockOnClose}>
        <div>Modal Content</div>
      </Modal>
    );
    
    expect(document.body.style.overflow).toBe('unset');
  });

  it('should reset body overflow on unmount', () => {
    const { unmount } = render(
      <Modal open={true} onClose={mockOnClose}>
        <div>Modal Content</div>
      </Modal>
    );
    
    expect(document.body.style.overflow).toBe('hidden');
    
    unmount();
    
    expect(document.body.style.overflow).toBe('unset');
  });

  it('should render complex children', () => {
    render(
      <Modal open={true} onClose={mockOnClose}>
        <h1>Title</h1>
        <p>Description</p>
        <button>Action</button>
      </Modal>
    );
    
    expect(screen.getByText('Title')).toBeInTheDocument();
    expect(screen.getByText('Description')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Action' })).toBeInTheDocument();
  });

  it('should have proper accessibility attributes on close button', () => {
    render(
      <Modal open={true} onClose={mockOnClose}>
        <div>Modal Content</div>
      </Modal>
    );
    
    const closeButton = screen.getByRole('button', { name: /close modal/i });
    expect(closeButton).toHaveAttribute('aria-label', 'Close modal');
  });
});
