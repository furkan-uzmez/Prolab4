// src/main/resources/static/js/script.js
let map, markers = [], infoWindows = [], baslangicMarker, hedefMarker, clickMarker, nearestMarker;
let currentRouteMarkers = [];
let currentRoutePolylines = [];
let animationInterval;

let route1Polylines = [];
let route2Polylines = [];
let route3Polylines = [];
let taxiPolylines = []; // Yeni taxi rotası için dizi

// Global variable to store route data
window.routeDataResult = null;

function initMap() {
    const baslangicEnlem = parseFloat(window.baslangicEnlem) || 40.7669;
    const baslangicBoylam = parseFloat(window.baslangicBoylam) || 29.9169;
    const hedefEnlem = parseFloat(window.hedefEnlem);
    const hedefBoylam = parseFloat(window.hedefBoylam);
    const durakVerisi = Array.isArray(window.durakVerisi) ? window.durakVerisi : [];
    const nearestStop = window.nearestStop || null;

    map = new google.maps.Map(document.getElementById('map'), {
        center: { lat: baslangicEnlem, lng: baslangicBoylam },
        zoom: 13,
        mapTypeControl: true,
        mapTypeControlOptions: {
            style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
            position: google.maps.ControlPosition.TOP_RIGHT
        },
        zoomControl: true,
        zoomControlOptions: { position: google.maps.ControlPosition.RIGHT_TOP },
        streetViewControl: true,
        streetViewControlOptions: { position: google.maps.ControlPosition.RIGHT_BOTTOM },
        fullscreenControl: true,
        fullscreenControlOptions: { position: google.maps.ControlPosition.BOTTOM_RIGHT },
        styles: document.body.classList.contains('dark-mode') ? [
            { elementType: "geometry", stylers: [{ color: "#242f3e" }] },
            { elementType: "labels.text.stroke", stylers: [{ color: "#242f3e" }] },
            { elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
            { featureType: "administrative.locality", elementType: "labels.text.fill", stylers: [{ color: "#d59563" }] },
            { featureType: "poi", elementType: "labels.text.fill", stylers: [{ color: "#d59563" }] },
            { featureType: "poi.park", elementType: "geometry", stylers: [{ color: "#263c3f" }] },
            { featureType: "poi.park", elementType: "labels.text.fill", stylers: [{ color: "#6b9a76" }] },
            { featureType: "road", elementType: "geometry", stylers: [{ color: "#38414e" }] },
            { featureType: "road", elementType: "geometry.stroke", stylers: [{ color: "#212a37" }] },
            { featureType: "road", elementType: "labels.text.fill", stylers: [{ color: "#9ca5b3" }] },
            { featureType: "road.highway", elementType: "geometry", stylers: [{ color: "#746855" }] },
            { featureType: "road.highway", elementType: "geometry.stroke", stylers: [{ color: "#1f2835" }] },
            { featureType: "road.highway", elementType: "labels.text.fill", stylers: [{ color: "#f3d19c" }] },
            { featureType: "transit", elementType: "geometry", stylers: [{ color: "#2f3948" }] },
            { featureType: "transit.station", elementType: "labels.text.fill", stylers: [{ color: "#d59563" }] },
            { featureType: "water", elementType: "geometry", stylers: [{ color: "#17263c" }] },
            { featureType: "water", elementType: "labels.text.fill", stylers: [{ color: "#515c6d" }] },
            { featureType: "water", elementType: "labels.text.stroke", stylers: [{ color: "#17263c" }] }
        ] : []
    });

    const bounds = new google.maps.LatLngBounds();

    durakVerisi.forEach(durak => {
        const iconUrl = durak.type === 'bus'
            ? 'https://img.icons8.com/color/48/000000/bus.png'
            : 'https://img.icons8.com/color/48/000000/tram.png';
        const marker = new google.maps.Marker({
            position: { lat: parseFloat(durak.lat), lng: parseFloat(durak.lot || durak.lon) },
            map: map,
            title: durak.name,
            icon: { url: iconUrl, scaledSize: new google.maps.Size(34, 34) },
            type: durak.type,
            animation: google.maps.Animation.DROP
        });
        const infoWindow = new google.maps.InfoWindow({
            content: `
                <div style="font-family: 'Poppins', sans-serif; padding: 10px;">
                    <h3 style="margin: 0; color: #4285F4;">${durak.name}</h3>
                    <p style="margin: 5px 0;">Tür: ${durak.type === 'bus' ? 'Otobüs' : 'Tramvay'}</p>
                    <p style="margin: 5px 0;">Konum: ${parseFloat(durak.lat).toFixed(6)}, ${(parseFloat(durak.lot || durak.lon)).toFixed(6)}</p>
                </div>`
        });
        marker.addListener('click', () => {
            infoWindows.forEach(window => window.close());
            infoWindow.open(map, marker);
            document.getElementById('status-bar').textContent = `Durum: ${durak.name} seçildi`;
            map.panTo(marker.getPosition());
        });
        marker.addListener('mouseover', () => infoWindow.open(map, marker));
        marker.addListener('mouseout', () => infoWindow.close());
        markers.push(marker);
        infoWindows.push(infoWindow);
        bounds.extend(marker.getPosition());
    });

    baslangicMarker = new google.maps.Marker({
        position: { lat: baslangicEnlem, lng: baslangicBoylam },
        map: map,
        title: 'Başlangıç Noktası',
        icon: { url: 'https://img.icons8.com/color/48/000000/marker.png', scaledSize: new google.maps.Size(36, 36) },
        animation: google.maps.Animation.DROP
    });
    bounds.extend(baslangicMarker.getPosition());

    if (hedefEnlem && hedefBoylam && !isNaN(hedefEnlem) && !isNaN(hedefBoylam)) {
        hedefMarker = new google.maps.Marker({
            position: { lat: hedefEnlem, lng: hedefBoylam },
            map: map,
            title: 'Hedef Noktası',
            icon: { url: 'https://img.icons8.com/color/48/000000/finish-flag.png', scaledSize: new google.maps.Size(36, 36) },
            animation: google.maps.Animation.DROP
        });
        bounds.extend(hedefMarker.getPosition());
    }

    if (nearestStop && nearestStop.lat && (nearestStop.lot || nearestStop.lon)) {
        nearestMarker = new google.maps.Marker({
            position: { lat: parseFloat(nearestStop.lat), lng: parseFloat(nearestStop.lot || nearestStop.lon) },
            map: map,
            title: nearestStop.name,
            icon: {
                url: nearestStop.type === 'bus' ? 'https://img.icons8.com/color/48/000000/bus.png' : 'https://img.icons8.com/color/48/000000/tram.png',
                scaledSize: new google.maps.Size(42, 42)
            },
            animation: google.maps.Animation.BOUNCE
        });
        const nearestInfoWindow = new google.maps.InfoWindow({
            content: `
                <div style="font-family: 'Poppins', sans-serif; padding: 10px;">
                    <h3 style="margin: 0; color: #4285F4;">${nearestStop.name}</h3>
                    <p style="margin: 5px 0;">Tür: ${nearestStop.type === 'bus' ? 'Otobüs' : 'Tramvay'}</p>
                    <p style="margin: 5px 0;">Konum: ${parseFloat(nearestStop.lat).toFixed(6)}, ${(parseFloat(nearestStop.lot || nearestStop.lon)).toFixed(6)}</p>
                    <p style="margin: 5px 0;">Mesafe: ${nearestStop.distance_text || 'Bilinmiyor'}</p>
                </div>`
        });
        nearestMarker.addListener('click', () => {
            infoWindows.forEach(window => window.close());
            nearestInfoWindow.open(map, nearestMarker);
            document.getElementById('status-bar').textContent = `Durum: En yakın durak ${nearestStop.name} seçildi`;
        });
        infoWindows.push(nearestInfoWindow);
        bounds.extend(nearestMarker.getPosition());
    }

    map.fitBounds(bounds);

    map.addListener('click', (event) => {
        placeClickMarker(event.latLng);
        highlightNearestStop(event.latLng);
        document.getElementById('status-bar').textContent = 'Durum: Yeni konum seçildi';
    });

    setupEventListeners();

    if (window.routeData) {
        initializeRoute(window.routeData);
    }

    if (hedefEnlem && hedefBoylam && nearestStop) {
        drawInitialRoute();
    }

    createParticles();
}

function drawInitialRoute() {
    clearPreviousRoutes();
    const baslangicEnlem = parseFloat(window.baslangicEnlem) || 40.7669;
    const baslangicBoylam = parseFloat(window.baslangicBoylam) || 29.9169;
    const hedefEnlem = parseFloat(window.hedefEnlem);
    const hedefBoylam = parseFloat(window.hedefBoylam);
    const nearestStop = window.nearestStop || null;

    if (nearestStop && hedefEnlem && hedefBoylam && !isNaN(hedefEnlem) && !isNaN(hedefBoylam)) {
        const transitPath = [
            { lat: baslangicEnlem, lng: baslangicBoylam },
            { lat: parseFloat(nearestStop.lat), lng: parseFloat(nearestStop.lot || nearestStop.lon) },
            { lat: hedefEnlem, lng: hedefBoylam }
        ];
        drawRoute(transitPath, '#4285F4');
    }
}

function placeClickMarker(location) {
    if (clickMarker) clickMarker.setMap(null);
    clickMarker = new google.maps.Marker({
        position: location,
        map: map,
        icon: { url: 'https://img.icons8.com/color/48/000000/map-pin.png', scaledSize: new google.maps.Size(34, 34) },
        animation: google.maps.Animation.DROP
    });
    document.getElementById('selected-coordinates').textContent = `${location.lat().toFixed(6)}, ${location.lng().toFixed(6)}`;
    map.panTo(location);
    clickMarker.addListener('click', () => {
        document.getElementById('status-bar').textContent = 'Durum: Seçilen konum: ' + document.getElementById('selected-coordinates').textContent;
    });
}

function clearPreviousRoutes() {
    currentRouteMarkers.forEach(marker => {
        if (marker && marker.setMap) marker.setMap(null);
    });
    currentRouteMarkers = [];

    currentRoutePolylines.forEach(polyline => {
        if (polyline && polyline.setMap) polyline.setMap(null);
    });
    currentRoutePolylines = [];
}

function drawRoute(path, color) {
    const validPath = path.filter(p => p && typeof p.lat === 'number' && !isNaN(p.lat) && typeof p.lng === 'number' && !isNaN(p.lng));
    if (validPath.length < 2) {
        document.getElementById('status-bar').textContent = 'Durum: Geçerli rota çizilemedi';
        return;
    }

    const routeLine = new google.maps.Polyline({
        path: validPath,
        geodesic: true,
        strokeColor: color,
        strokeOpacity: 1.0,
        strokeWeight: 6,
        map: map,
        icons: [{
            icon: { path: google.maps.SymbolPath.CIRCLE, scale: 3, fillColor: '#ffffff', fillOpacity: 1, strokeColor: color },
            offset: '0%',
            repeat: '20px'
        }]
    });

    currentRoutePolylines.push(routeLine);

    const bounds = new google.maps.LatLngBounds();
    validPath.forEach(point => bounds.extend(point));
    map.fitBounds(bounds);

    let totalDistance = 0;
    for (let i = 0; i < validPath.length - 1; i++) {
        totalDistance += google.maps.geometry.spherical.computeDistanceBetween(
            new google.maps.LatLng(validPath[i].lat, validPath[i].lng),
            new google.maps.LatLng(validPath[i + 1].lat, validPath[i + 1].lng)
        );
    }
    const distanceInKm = (totalDistance / 1000).toFixed(2);
    document.getElementById('status-bar').innerHTML = `Durum: Rota çizildi<br>Mesafe: ${distanceInKm} km`;
}

function highlightNearestStop(startPos) {
    if (nearestMarker) {
        nearestMarker.setAnimation(null);
        if (nearestMarker.originalIcon) nearestMarker.setIcon(nearestMarker.originalIcon);
        clearInterval(animationInterval);
    }

    let minDistance = Infinity;
    let nearestStop = null;

    markers.forEach(marker => {
        const markerPos = marker.getPosition();
        const distance = google.maps.geometry.spherical.computeDistanceBetween(
            new google.maps.LatLng(startPos.lat(), startPos.lng()),
            markerPos
        );
        if (distance < minDistance) {
            minDistance = distance;
            nearestStop = marker;
        }
    });

    if (nearestStop) {
        nearestMarker = nearestStop;
        nearestMarker.originalIcon = nearestMarker.getIcon() || {
            url: nearestMarker.type === 'bus' ? 'https://img.icons8.com/color/48/000000/bus.png' : 'https://img.icons8.com/color/48/000000/tram.png',
            scaledSize: new google.maps.Size(34, 34)
        };
        nearestMarker.setIcon({
            url: nearestMarker.originalIcon.url,
            scaledSize: new google.maps.Size(42, 42)
        });
        nearestMarker.setAnimation(google.maps.Animation.BOUNCE);

        animationInterval = setInterval(() => {
            const currentSize = nearestMarker.getIcon().scaledSize.width;
            nearestMarker.setIcon({
                url: nearestMarker.originalIcon.url,
                scaledSize: new google.maps.Size(currentSize === 42 ? 34 : 42, currentSize === 42 ? 34 : 42)
            });
        }, 800);

        document.getElementById('status-bar').textContent = `Durum: En yakın durak: ${nearestStop.title} (${(minDistance / 1000).toFixed(2)} km)`;
    }
}

function filterMarkers(type) {
    markers.forEach(marker => {
        if (type === 'all' || marker.type === type) {
            marker.setVisible(true);
        } else {
            marker.setVisible(false);
        }
    });
    document.getElementById('status-bar').textContent = `Durum: ${type === 'all' ? 'Tüm duraklar gösteriliyor' : 'Sadece ' + (type === 'bus' ? 'otobüs' : 'tramvay') + ' durakları gösteriliyor'}`;
}

function saveRoute() {
    const route = {
        baslangic: baslangicMarker ? { lat: baslangicMarker.getPosition().lat(), lng: baslangicMarker.getPosition().lng() } : null,
        hedef: hedefMarker ? { lat: hedefMarker.getPosition().lat(), lng: hedefMarker.getPosition().lng() } : null,
        waypoints: currentRoutePolylines.length > 0 ? currentRoutePolylines[0].getPath().getArray().map(point => ({ lat: point.lat(), lng: point.lng() })) : []
    };
    const routeJSON = JSON.stringify(route);
    const blob = new Blob([routeJSON], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'route_' + new Date().toISOString().split('T')[0] + '.json';
    a.click();
    URL.revokeObjectURL(url);
    document.getElementById('status-bar').textContent = 'Durum: Rota kaydedildi';
}

document.querySelectorAll('.tab-btn').forEach(button => {
    button.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));
        button.classList.add('active');
        const tabId = button.getAttribute('data-tab');
        document.getElementById(tabId).classList.add('active');
        document.getElementById('status-bar').textContent = `Durum: ${button.textContent} sekmesi açık`;
    });
});

document.getElementById('homeForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const button = e.target.querySelector('button');
    button.textContent = 'Yükleniyor...';
    button.disabled = true;
    document.getElementById('status-bar').textContent = 'Durum: Rota hesaplanıyor...';
});

function addRouteToggleListeners() {
    document.getElementById('show-route-1').addEventListener('click', () => toggleRoute("1"));
    document.getElementById('show-route-2').addEventListener('click', () => toggleRoute("2"));
    document.getElementById('show-route-3').addEventListener('click', () => toggleRoute("3"));
    document.getElementById('show-all-routes').addEventListener('click', () => toggleRoute("all"));
    document.getElementById('show-taxi-route').addEventListener('click', () => toggleRoute("taxi")); // Yeni taxi tuşu
}

function setupEventListeners() {
    addRouteToggleListeners();

    document.getElementById('modeToggle').addEventListener('click', () => {
        document.body.classList.toggle('dark-mode');
        const isDark = document.body.classList.contains('dark-mode');
        document.getElementById('modeToggle').textContent = isDark ? '🌙' : '☀';
        document.getElementById('theme-info').textContent = `Tema: ${isDark ? 'Karanlık Mod' : 'Açık Mod'}`;
        document.getElementById('status-bar').textContent = `Durum: ${isDark ? 'Karanlık' : 'Açık'} mod aktif`;
        map.setOptions({
            styles: isDark ? [
                { elementType: "geometry", stylers: [{ color: "#242f3e" }] },
                { elementType: "labels.text.stroke", stylers: [{ color: "#242f3e" }] },
                { elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
                { featureType: "administrative.locality", elementType: "labels.text.fill", stylers: [{ color: "#d59563" }] },
                { featureType: "poi", elementType: "labels.text.fill", stylers: [{ color: "#d59563" }] },
                { featureType: "poi.park", elementType: "geometry", stylers: [{ color: "#263c3f" }] },
                { featureType: "poi.park", elementType: "labels.text.fill", stylers: [{ color: "#6b9a76" }] },
                { featureType: "road", elementType: "geometry", stylers: [{ color: "#38414e" }] },
                { featureType: "road", elementType: "geometry.stroke", stylers: [{ color: "#212a37" }] },
                { featureType: "road", elementType: "labels.text.fill", stylers: [{ color: "#9ca5b3" }] },
                { featureType: "road.highway", elementType: "geometry", stylers: [{ color: "#746855" }] },
                { featureType: "road.highway", elementType: "geometry.stroke", stylers: [{ color: "#1f2835" }] },
                { featureType: "road.highway", elementType: "labels.text.fill", stylers: [{ color: "#f3d19c" }] },
                { featureType: "transit", elementType: "geometry", stylers: [{ color: "#2f3948" }] },
                { featureType: "transit.station", elementType: "labels.text.fill", stylers: [{ color: "#d59563" }] },
                { featureType: "water", elementType: "geometry", stylers: [{ color: "#17263c" }] },
                { featureType: "water", elementType: "labels.text.fill", stylers: [{ color: "#515c6d" }] },
                { featureType: "water", elementType: "labels.text.stroke", stylers: [{ color: "#17263c" }] }
            ] : []
        });
    });

    document.getElementById('resetMap').addEventListener('click', () => {
        map.setCenter({ lat: parseFloat(window.baslangicEnlem) || 40.7669, lng: parseFloat(window.baslangicBoylam) || 29.9169 });
        map.setZoom(13);
        document.getElementById('status-bar').textContent = 'Durum: Harita sıfırlandı';
    });

    document.getElementById('getLocation').addEventListener('click', () => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const pos = { lat: position.coords.latitude, lng: position.coords.longitude };
                    placeClickMarker(pos);
                    map.setCenter(pos);
                    map.setZoom(15);
                    highlightNearestStop(pos);
                    document.getElementById('status-bar').textContent = 'Durum: Mevcut konum bulundu';
                },
                (error) => {
                    alert('Konum alınamadı: ' + error.message);
                    document.getElementById('status-bar').textContent = 'Durum: Konum alınamadı';
                }
            );
        } else {
            alert('Tarayıcınız konumu desteklemiyor.');
            document.getElementById('status-bar').textContent = 'Durum: Konum desteklenmiyor';
        }
    });

    document.getElementById('zoomIn').addEventListener('click', () => {
        map.setZoom(map.getZoom() + 1);
        document.getElementById('status-bar').textContent = `Durum: Yakınlaştırma (${map.getZoom()})`;
    });

    document.getElementById('zoomOut').addEventListener('click', () => {
        map.setZoom(map.getZoom() - 1);
        document.getElementById('status-bar').textContent = `Durum: Uzaklaştırma (${map.getZoom()})`;
    });

    document.getElementById('clearMarkers').addEventListener('click', () => {
        if (clickMarker) {
            clickMarker.setMap(null);
            clickMarker = null;
            document.getElementById('selected-coordinates').textContent = 'Henüz seçilmedi';
            if (nearestMarker) {
                nearestMarker.setAnimation(null);
                nearestMarker.setIcon(nearestMarker.originalIcon);
                clearInterval(animationInterval);
            }
            document.getElementById('status-bar').textContent = 'Durum: İşaretler temizlendi';
        }
        clearPreviousRoutes();
    });

    document.getElementById('set-start').addEventListener('click', () => {
        if (clickMarker) {
            const pos = clickMarker.getPosition();
            document.getElementById('baslangic_enlem').value = pos.lat().toFixed(6);
            document.getElementById('baslangic_boylam').value = pos.lng().toFixed(6);
            baslangicMarker.setPosition(pos);
            animateMarker(baslangicMarker);
            highlightNearestStop(pos);
            document.getElementById('status-bar').textContent = 'Durum: Başlangıç noktası ayarlandı';
        }
    });

    document.getElementById('set-destination').addEventListener('click', () => {
        if (clickMarker) {
            const pos = clickMarker.getPosition();
            document.getElementById('hedef_enlem').value = pos.lat().toFixed(6);
            document.getElementById('hedef_boylam').value = pos.lng().toFixed(6);
            if (!hedefMarker) {
                hedefMarker = new google.maps.Marker({
                    position: pos,
                    map: map,
                    title: 'Hedef Noktası',
                    icon: { url: 'https://img.icons8.com/color/48/000000/finish-flag.png', scaledSize: new google.maps.Size(36, 36) },
                    animation: google.maps.Animation.DROP
                });
            } else {
                hedefMarker.setPosition(pos);
            }
            animateMarker(hedefMarker);
            document.getElementById('status-bar').textContent = 'Durum: Hedef noktası ayarlandı';
        }
    });

    document.getElementById('filter-bus').addEventListener('click', () => filterMarkers('bus'));
    document.getElementById('filter-tram').addEventListener('click', () => filterMarkers('tram'));
    document.getElementById('filter-all').addEventListener('click', () => filterMarkers('all'));
    document.getElementById('saveRoute').addEventListener('click', saveRoute);

    document.getElementById('locationForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const button = e.target.querySelector('button');
        button.textContent = 'Yükleniyor...';
        button.disabled = true;
        document.getElementById('status-bar').textContent = 'Durum: Rota hesaplanıyor...';

        const formData = new FormData(e.target);
        fetch('/', {
            method: 'POST',
            body: formData,
            headers: { 'Accept': 'application/json' }
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(`Server error: ${response.status} - ${text}`);
                    });
                }
                return response.json();
            })
            .then(sonuc => {
                console.log("Fetch result:", sonuc);
                button.textContent = 'Konumları Göster';
                button.disabled = false;
                initializeRoute(sonuc);
            })
            .catch(error => {
                console.error('Fetch error:', error);
                button.textContent = 'Konumları Göster';
                button.disabled = false;
                document.getElementById('status-bar').textContent = 'Durum: Hata oluştu: ' + error.message;
            });
    });

    document.getElementById('searchButton').addEventListener('click', () => {
        const query = document.getElementById('searchInput').value.trim();
        if (query) {
            searchLocation(query);
        }
    });

    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            const query = document.getElementById('searchInput').value.trim();
            if (query) {
                searchLocation(query);
            }
        }
    });
}

function initializeRoute(sonuc) {
    console.log("initializeRoute called with:", JSON.stringify(sonuc, null, 2));

    if (!sonuc || typeof sonuc !== 'object') {
        document.getElementById('status-bar').textContent = 'Durum: Geçersiz rota verisi';
        return;
    }

    // Store the route data globally
    window.routeDataResult = sonuc;

    clearPreviousRoutes();
    route1Polylines = [];
    route2Polylines = [];
    route3Polylines = [];
    taxiPolylines = [];

    const routeColors = {
        "1": "#4285F4", // Blue
        "2": "#FFA500", // Orange
        "3": "#34C759", // Green
        "taxi": "#FF0000" // Red (taxi)
    };

    let bounds = new google.maps.LatLngBounds();
    let routeCount = 0;

    for (const routeId in sonuc) {
        console.log(`Processing routeId: ${routeId}`);
        if (sonuc[routeId] && Array.isArray(sonuc[routeId].coordinates)) {
            const coordinates = sonuc[routeId].coordinates;
            const routePath = coordinates.map(coord => ({
                lat: parseFloat(coord.lat),
                lng: parseFloat(coord.lon || coord.lot || coord.lng)
            }));

            if (routePath.length > 0) {
                routeCount++;
                const color = routeColors[routeId] || '#4285F4';
                console.log(`Route ${routeId} assigned color: ${color}`);
                const polyline = drawRouteWithId(routePath, color, routeId);

                if (routeId === "1") route1Polylines.push(polyline);
                else if (routeId === "2") route2Polylines.push(polyline);
                else if (routeId === "3") route3Polylines.push(polyline);
                else if (routeId === "taxi") taxiPolylines.push(polyline);
                else console.warn(`Unknown routeId: ${routeId}`);

                if (routeId === "1" && routePath.length > 0) {
                    baslangicMarker.setPosition(routePath[0]);
                    if (routePath.length > 1) {
                        if (!hedefMarker) {
                            hedefMarker = new google.maps.Marker({
                                position: routePath[routePath.length - 1],
                                map: map,
                                title: 'Hedef Noktası',
                                icon: { url: 'https://img.icons8.com/color/48/000000/finish-flag.png', scaledSize: new google.maps.Size(36, 36) }
                            });
                        } else {
                            hedefMarker.setPosition(routePath[routePath.length - 1]);
                        }
                    }
                }

                routePath.forEach(point => bounds.extend(point));
            }
        }
    }

    console.log(`Route counts - 1: ${route1Polylines.length}, 2: ${route2Polylines.length}, 3: ${route3Polylines.length}, taxi: ${taxiPolylines.length}`);
    if (routeCount > 0) {
        map.fitBounds(bounds);
        document.getElementById('status-bar').textContent = `Durum: ${routeCount} rota çizildi`;
    } else {
        document.getElementById('status-bar').textContent = 'Durum: Geçerli rota bulunamadı';
    }
}

function drawRouteWithId(path, color, routeId) {
    const validPath = path.filter(p => p && typeof p.lat === 'number' && !isNaN(p.lat) && typeof p.lng === 'number' && !isNaN(p.lng));
    if (validPath.length < 2) {
        document.getElementById('status-bar').textContent = 'Durum: Geçerli rota çizilemedi';
        return null;
    }

    console.log(`Drawing route ${routeId} with color ${color}`);

    const routeLine = new google.maps.Polyline({
        path: validPath,
        geodesic: true,
        strokeColor: color,
        strokeOpacity: 1.0,
        strokeWeight: 6,
        map: map,
        routeId: routeId,
        icons: [{
            icon: { path: google.maps.SymbolPath.CIRCLE, scale: 3, fillColor: '#ffffff', fillOpacity: 1, strokeColor: color },
            offset: '0%',
            repeat: '20px'
        }]
    });

    currentRoutePolylines.push(routeLine);
    return routeLine;
}

function toggleRoute(routeId) {
    const routeColors = {
        "1": "#4285F4", // Blue
        "2": "#FFA500", // Orange
        "3": "#34C759", // Green
        "taxi": "#FF0000" // Red (taxi)
    };

    console.log(`Toggling route: ${routeId}`);

    // Hide all routes initially
    route1Polylines.forEach(polyline => polyline.setVisible(false));
    route2Polylines.forEach(polyline => polyline.setVisible(false));
    route3Polylines.forEach(polyline => polyline.setVisible(false));
    taxiPolylines.forEach(polyline => polyline.setVisible(false));

    // Show selected routes and update status bar
    let statusMessage = "";
    if (routeId === "all") {
        route1Polylines.forEach(polyline => {
            polyline.setVisible(true);
            polyline.setOptions({ strokeColor: routeColors["1"] });
        });
        route2Polylines.forEach(polyline => {
            polyline.setVisible(true);
            polyline.setOptions({ strokeColor: routeColors["2"] });
        });
        route3Polylines.forEach(polyline => {
            polyline.setVisible(true);
            polyline.setOptions({ strokeColor: routeColors["3"] });
        });
        taxiPolylines.forEach(polyline => {
            polyline.setVisible(true);
            polyline.setOptions({ strokeColor: routeColors["taxi"] });
        });
        statusMessage = 'Durum: Tüm rotalar gösteriliyor';
    } else {
        const routePolylines = routeId === "1" ? route1Polylines :
            routeId === "2" ? route2Polylines :
                routeId === "3" ? route3Polylines :
                    routeId === "taxi" ? taxiPolylines : [];
        routePolylines.forEach(polyline => {
            polyline.setVisible(true);
            polyline.setOptions({ strokeColor: routeColors[routeId] });
            console.log(`Route ${routeId} set to ${routeColors[routeId]}`);
        });

        // Get route details from window.routeDataResult
        if (window.routeDataResult && window.routeDataResult[routeId]) {
            const routeData = window.routeDataResult[routeId];
            let distance, cost, duration;
            if (routeId === "taxi") {
                distance = routeData.mesafe_km ? `${routeData.mesafe_km.toFixed(2)} km` : "N/A";
                cost = routeData.ucret ? `${routeData.ucret.toFixed(2)} TL` : "N/A";
                duration = routeData.tahmini_sure_dk ? `${Math.round(routeData.tahmini_sure_dk)} dk` : "N/A";
            } else {
                distance = routeData.toplam_mesafe_km ? `${routeData.toplam_mesafe_km} km` : "N/A";
                cost = routeData.toplam_ucret ? `${routeData.toplam_ucret} TL` : "N/A";
                duration = routeData.toplam_sure_dk ? `${routeData.toplam_sure_dk} dk` : "N/A";
            }
            statusMessage = `${routeId === "taxi" ? "Taksi" : "Rota " + routeId}: Mesafe: ${distance}, Ücret: ${cost}, Süre: ${duration}`;
        } else {
            statusMessage = `Durum: ${routeId === "taxi" ? "Taksi" : "Rota " + routeId} gösteriliyor (Detaylar mevcut değil)`;
        }
    }

    document.getElementById('status-bar').textContent = statusMessage;
}

function searchLocation(query) {
    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ address: query }, (results, status) => {
        if (status === google.maps.GeocoderStatus.OK && results[0]) {
            const location = results[0].geometry.location;
            map.setCenter(location);
            map.setZoom(14);
            placeClickMarker(location);
            document.getElementById('status-bar').textContent = `Durum: "${results[0].formatted_address}" bulundu`;
        } else {
            document.getElementById('status-bar').textContent = `Durum: "${query}" bulunamadı`;
            alert(`Lokasyon bulunamadı: ${query}`);
        }
    });
}

function animateMarker(marker) {
    marker.setAnimation(google.maps.Animation.BOUNCE);
    setTimeout(() => marker.setAnimation(null), 2000);
}

function createParticles() {
    const particleCount = 20;
    for (let i = 0; i < particleCount; i++) {
        const particle = new google.maps.Marker({
            position: map.getCenter(),
            map: map,
            icon: {
                url: 'https://img.icons8.com/color/48/000000/star.png',
                scaledSize: new google.maps.Size(10, 10)
            },
            animation: google.maps.Animation.DROP
        });
        setTimeout(() => {
            particle.setMap(null);
        }, 2000);
    }
}