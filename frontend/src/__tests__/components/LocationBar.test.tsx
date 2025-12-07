/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import LocationBar from '../../components/LocationBar';
import type { Location } from '../../types/Location';
import type { Weather } from '../../types/Weather';

// Mock the icons
jest.mock('react-bootstrap-icons', () => ({
  Crosshair: () => <div>Crosshair</div>,
  PencilSquare: () => <div>PencilSquare</div>,
}));

describe('LocationBar Component', () => {
  const mockOnLocationClick = jest.fn();
  const mockOnEditClick = jest.fn();

  const mockCurrentLocationWeather: Weather = {
    location: 'Current Location',
    latitude: 0,
    longitude: 0,
    timezone: 'UTC',
    current: {
      time: '2024-01-01T12:00:00Z',
      temperature: 20,
      weatherCode: '0',
      windSpeed: 10,
      windDirection: 'N',
      humidity: 50,
      precipitation: 0,
      precipitationProbability: 0,
    },
    hourly: [],
    daily: [{
      time: '2024-01-01',
      weatherCode: '0',
      temperatureMax: 25,
      temperatureMin: 15,
      sunRise: '2024-01-01T06:00:00Z',
      sunSet: '2024-01-01T18:00:00Z',
      precipitationProbabilityMax: 10,
      precipitation: 0,
      windSpeed: 10,
      windDirection: 'N',
    }],
  };

  const mockSavedLocations: Location[] = [
    {
      id: 1,
      name: 'Very Long Location Name That Should Be Truncated',
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

  const mockSavedWeatherData = new Map<number, Weather>([
    [1, { ...mockCurrentLocationWeather, location: 'New York' }],
    [2, { ...mockCurrentLocationWeather, location: 'Paris', current: { ...mockCurrentLocationWeather.current, temperature: 15 } }],
  ]);

  const mockLoadingWeather = new Set<number>();

  beforeEach(() => {
    mockOnLocationClick.mockClear();
    mockOnEditClick.mockClear();
  });

  it('should render current location button', () => {
    render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={[]}
        savedWeatherData={new Map()}
        loadingWeather={new Set()}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    expect(screen.getByText('Current Location')).toBeInTheDocument();
  });

  it('should render saved location cards', () => {
    render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={mockSavedLocations}
        savedWeatherData={mockSavedWeatherData}
        loadingWeather={mockLoadingWeather}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    expect(screen.getByText('Very Long Location Name That Should Be Truncated')).toBeInTheDocument();
    expect(screen.getByText('Paris')).toBeInTheDocument();
  });

  it('should render edit button', () => {
    render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={mockSavedLocations}
        savedWeatherData={mockSavedWeatherData}
        loadingWeather={mockLoadingWeather}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    expect(screen.getByText('Edit')).toBeInTheDocument();
  });

  it('should call onLocationClick when current location button is clicked', () => {
    render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={[]}
        savedWeatherData={new Map()}
        loadingWeather={new Set()}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    fireEvent.click(screen.getByText('Current Location'));
    expect(mockOnLocationClick).toHaveBeenCalledWith(null);
  });

  it('should call onLocationClick when saved location is clicked', () => {
    render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={mockSavedLocations}
        savedWeatherData={mockSavedWeatherData}
        loadingWeather={mockLoadingWeather}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    fireEvent.click(screen.getByText('Paris'));
    expect(mockOnLocationClick).toHaveBeenCalledWith(2);
  });

  it('should call onEditClick when edit button is clicked', () => {
    render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={[]}
        savedWeatherData={new Map()}
        loadingWeather={new Set()}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    fireEvent.click(screen.getByText('Edit'));
    expect(mockOnEditClick).toHaveBeenCalledTimes(1);
  });

  it('should highlight selected location', () => {
    const { container } = render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={mockSavedLocations}
        savedWeatherData={mockSavedWeatherData}
        loadingWeather={mockLoadingWeather}
        selectedLocationId={1}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    // Check that the selected location has the blue background class
    const locationButton = screen.getByText('Very Long Location Name That Should Be Truncated').closest('button');
    expect(locationButton).toHaveClass('bg-blue-500');
  });

  it('should show loading state for locations being fetched', () => {
    const loadingSet = new Set([1]);
    render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={mockSavedLocations}
        savedWeatherData={new Map()}
        loadingWeather={loadingSet}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    // The loading spinner should be present
    const buttons = screen.getAllByRole('button');
    const locationButton = buttons.find(btn => 
      btn.textContent?.includes('Very Long Location Name That Should Be Truncated')
    );
    expect(locationButton?.querySelector('.animate-spin')).toBeInTheDocument();
  });

  it('should show temperature for locations with weather data', () => {
    render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={mockSavedLocations}
        savedWeatherData={mockSavedWeatherData}
        loadingWeather={mockLoadingWeather}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    // Current location shows 20°C and saved location 1 shows 20°C (2 instances)
    const temp20Elements = screen.getAllByText('20°C');
    expect(temp20Elements.length).toBe(2);
    expect(screen.getByText('15°C')).toBeInTheDocument();
  });

  it('should not show drag handles or delete buttons', () => {
    const { container } = render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={mockSavedLocations}
        savedWeatherData={mockSavedWeatherData}
        loadingWeather={mockLoadingWeather}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    // No drag handles should be present
    expect(container.querySelector('[aria-label="Drag to reorder"]')).not.toBeInTheDocument();
    // No delete buttons should be present
    expect(container.querySelector('[aria-label="Remove location"]')).not.toBeInTheDocument();
  });

  it('should apply consistent width to all location cards', () => {
    const { container } = render(
      <LocationBar
        currentLocationWeather={mockCurrentLocationWeather}
        savedLocations={mockSavedLocations}
        savedWeatherData={mockSavedWeatherData}
        loadingWeather={mockLoadingWeather}
        selectedLocationId={null}
        onLocationClick={mockOnLocationClick}
        onEditClick={mockOnEditClick}
      />
    );

    // All cards should have w-[120px] class
    const buttons = container.querySelectorAll('button.w-\\[120px\\]');
    // Current location + 2 saved locations + edit button = 4
    expect(buttons.length).toBe(4);
  });
});
