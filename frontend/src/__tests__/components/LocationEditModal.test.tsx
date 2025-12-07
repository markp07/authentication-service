/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import LocationEditModal from '../../components/LocationEditModal';
import type { Location } from '../../types/Location';

// Mock the child components
jest.mock('../../components/Modal', () => {
  return function MockModal({ open, onClose, children }: any) {
    if (!open) return null;
    return (
      <div data-testid="modal">
        <button onClick={onClose} aria-label="Close modal">×</button>
        {children}
      </div>
    );
  };
});

jest.mock('../../components/LocationSearch', () => {
  return function MockLocationSearch({ onLocationSelect }: any) {
    return (
      <div data-testid="location-search">
        <button onClick={() => onLocationSelect({ id: 3, name: 'Tokyo', latitude: 35.6762, longitude: 139.6503, country: 'Japan', admin1: '', countryCode: 'JP' })}>
          Select Tokyo
        </button>
      </div>
    );
  };
});

// Mock the icons
jest.mock('@tabler/icons-react', () => ({
  IconX: () => <div>X</div>,
  IconMapPin: () => <div>MapPin</div>,
  IconGripVertical: () => <div>Grip</div>,
  IconPlus: () => <div>Plus</div>,
}));

// Mock DnD Kit
jest.mock('@dnd-kit/core', () => ({
  DndContext: ({ children }: any) => <div>{children}</div>,
  closestCenter: jest.fn(),
  KeyboardSensor: jest.fn(),
  PointerSensor: jest.fn(),
  useSensor: jest.fn(),
  useSensors: jest.fn(() => []),
}));

jest.mock('@dnd-kit/sortable', () => ({
  arrayMove: jest.fn((arr, oldIndex, newIndex) => {
    const newArr = [...arr];
    const [removed] = newArr.splice(oldIndex, 1);
    newArr.splice(newIndex, 0, removed);
    return newArr;
  }),
  SortableContext: ({ children }: any) => <div>{children}</div>,
  sortableKeyboardCoordinates: jest.fn(),
  useSortable: jest.fn(() => ({
    attributes: {},
    listeners: {},
    setNodeRef: jest.fn(),
    transform: null,
    transition: null,
    isDragging: false,
  })),
  verticalListSortingStrategy: jest.fn(),
}));

describe('LocationEditModal Component', () => {
  const mockOnClose = jest.fn();
  const mockOnRemoveLocation = jest.fn();
  const mockOnReorderLocations = jest.fn();
  const mockOnAddLocation = jest.fn();

  const mockLocations: Location[] = [
    {
      id: 1,
      name: 'New York',
      latitude: 40.7128,
      longitude: -74.0060,
      country: 'USA',
      admin1: 'NY',
      countryCode: 'US',
    },
    {
      id: 2,
      name: 'Paris',
      latitude: 48.8566,
      longitude: 2.3522,
      country: 'France',
      admin1: 'Île-de-France',
      countryCode: 'FR',
    },
  ];

  beforeEach(() => {
    mockOnClose.mockClear();
    mockOnRemoveLocation.mockClear();
    mockOnReorderLocations.mockClear();
    mockOnAddLocation.mockClear();
  });

  it('should not render when open is false', () => {
    render(
      <LocationEditModal
        open={false}
        onClose={mockOnClose}
        locations={mockLocations}
        onRemoveLocation={mockOnRemoveLocation}
        onReorderLocations={mockOnReorderLocations}
        onAddLocation={mockOnAddLocation}
        weatherApiBase="http://localhost"
      />
    );

    expect(screen.queryByText('Manage Locations')).not.toBeInTheDocument();
  });

  it('should render when open is true', () => {
    render(
      <LocationEditModal
        open={true}
        onClose={mockOnClose}
        locations={mockLocations}
        onRemoveLocation={mockOnRemoveLocation}
        onReorderLocations={mockOnReorderLocations}
        onAddLocation={mockOnAddLocation}
        weatherApiBase="http://localhost"
      />
    );

    expect(screen.getByText('Manage Locations')).toBeInTheDocument();
  });

  it('should render add location button', () => {
    render(
      <LocationEditModal
        open={true}
        onClose={mockOnClose}
        locations={mockLocations}
        onRemoveLocation={mockOnRemoveLocation}
        onReorderLocations={mockOnReorderLocations}
        onAddLocation={mockOnAddLocation}
        weatherApiBase="http://localhost"
      />
    );

    expect(screen.getByText('Add New Location')).toBeInTheDocument();
  });

  it('should show location search when add button is clicked', () => {
    render(
      <LocationEditModal
        open={true}
        onClose={mockOnClose}
        locations={mockLocations}
        onRemoveLocation={mockOnRemoveLocation}
        onReorderLocations={mockOnReorderLocations}
        onAddLocation={mockOnAddLocation}
        weatherApiBase="http://localhost"
      />
    );

    fireEvent.click(screen.getByText('Add New Location'));
    expect(screen.getByText('Search Location')).toBeInTheDocument();
    expect(screen.getByTestId('location-search')).toBeInTheDocument();
  });

  it('should call onAddLocation when a location is selected from search', () => {
    render(
      <LocationEditModal
        open={true}
        onClose={mockOnClose}
        locations={mockLocations}
        onRemoveLocation={mockOnRemoveLocation}
        onReorderLocations={mockOnReorderLocations}
        onAddLocation={mockOnAddLocation}
        weatherApiBase="http://localhost"
      />
    );

    fireEvent.click(screen.getByText('Add New Location'));
    fireEvent.click(screen.getByText('Select Tokyo'));
    
    expect(mockOnAddLocation).toHaveBeenCalledWith({
      id: 3,
      name: 'Tokyo',
      latitude: 35.6762,
      longitude: 139.6503,
      country: 'Japan',
      admin1: '',
      countryCode: 'JP',
    });
  });

  it('should render saved locations list', () => {
    render(
      <LocationEditModal
        open={true}
        onClose={mockOnClose}
        locations={mockLocations}
        onRemoveLocation={mockOnRemoveLocation}
        onReorderLocations={mockOnReorderLocations}
        onAddLocation={mockOnAddLocation}
        weatherApiBase="http://localhost"
      />
    );

    expect(screen.getByText('New York')).toBeInTheDocument();
    expect(screen.getByText('Paris')).toBeInTheDocument();
    expect(screen.getByText('Saved Locations (2/5)')).toBeInTheDocument();
  });

  it('should show empty state when no locations are saved', () => {
    render(
      <LocationEditModal
        open={true}
        onClose={mockOnClose}
        locations={[]}
        onRemoveLocation={mockOnRemoveLocation}
        onReorderLocations={mockOnReorderLocations}
        onAddLocation={mockOnAddLocation}
        weatherApiBase="http://localhost"
      />
    );

    expect(screen.getByText('No Saved Locations')).toBeInTheDocument();
    expect(screen.getByText('Search and add up to 5 locations to view their weather')).toBeInTheDocument();
  });

  it('should show drag and drop hint text', () => {
    render(
      <LocationEditModal
        open={true}
        onClose={mockOnClose}
        locations={mockLocations}
        onRemoveLocation={mockOnRemoveLocation}
        onReorderLocations={mockOnReorderLocations}
        onAddLocation={mockOnAddLocation}
        weatherApiBase="http://localhost"
      />
    );

    expect(screen.getByText('Drag and drop locations to reorder them. Click the X button to remove a location.')).toBeInTheDocument();
  });
});
