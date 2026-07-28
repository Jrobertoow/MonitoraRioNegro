"use strict";

const STORAGE_KEYS = {
    stations: "monitoraRioNegro.stations.v1",
    measurements: "monitoraRioNegro.measurements.v1",
    alerts: "monitoraRioNegro.alerts.v1"
};

const DEFAULT_LIMITS = Object.freeze({
    dry: 15,
    attention: 27,
    flood: 28,
    emergency: 29
});

const STATUS_LABELS = Object.freeze({
    SECA: "Seca",
    NORMAL: "Normal",
    ATENCAO: "Atenção",
    CHEIA: "Cheia",
    EMERGENCIA: "Emergência"
});

const SECTION_TITLES = Object.freeze({
    dashboard: "PAINEL GERAL",
    estacoes: "ESTAÇÕES DE MONITORAMENTO",
    medicoes: "MEDIÇÕES DIÁRIAS",
    historico: "HISTÓRICO E VARIAÇÃO",
    comparacao: "COMPARAÇÃO ENTRE ESTAÇÕES",
    alertas: "ALERTAS HIDROLÓGICOS"
});

const state = {
    stations: loadStorage(STORAGE_KEYS.stations),
    measurements: loadStorage(STORAGE_KEYS.measurements),
    alerts: loadStorage(STORAGE_KEYS.alerts),
    confirmAction: null
};

const elements = {};

document.addEventListener("DOMContentLoaded", () => {
    cacheElements();
    bindEvents();
    setTodayAsDefault();
    renderAll();
});

function cacheElements() {
    const ids = [
        "sidebar", "menuToggle", "pageTitle", "toastContainer", "loadDemoButton",
        "quickMeasurementButton", "newStationButton", "newMeasurementButton",
        "statStations", "statMeasurements", "statAverage", "statStatus",
        "statStatusDetail", "currentStatusCard", "dashboardStationFilter", "levelChart",
        "chartEmptyState", "latestMeasurement", "recentMeasurementsTable", "stationCards",
        "stationsEmptyState", "measurementStationFilter", "measurementStatusFilter",
        "measurementSearch", "measurementsTable", "measurementsEmptyState", "historyStationFilter",
        "historyOrderFilter", "historySummary", "historyChart", "historyTable", "exportCsvButton",
        "compareStationA", "compareStationB", "compareButton", "comparisonResult", "alertsList",
        "alertsEmptyState", "clearAlertsButton", "stationModal", "stationForm", "stationModalTitle",
        "stationId", "stationName", "stationCity", "stationLocation", "stationClassifier",
        "customLevels", "dryLimit", "attentionLimit", "floodLimit", "emergencyLimit",
        "stationFormError", "measurementModal", "measurementForm", "measurementModalTitle",
        "measurementId", "measurementStation", "measurementDate", "measurementLevel",
        "measurementRain", "measurementTemperature", "measurementObservation", "measurementFormError",
        "confirmModal", "confirmForm", "confirmTitle", "confirmMessage", "confirmActionButton"
    ];

    ids.forEach((id) => {
        elements[id] = document.getElementById(id);
    });
}

function bindEvents() {
    document.querySelectorAll(".nav-item").forEach((button) => {
        button.addEventListener("click", () => navigateTo(button.dataset.section));
    });

    document.querySelectorAll("[data-go]").forEach((button) => {
        button.addEventListener("click", () => navigateTo(button.dataset.go));
    });

    elements.menuToggle.addEventListener("click", () => {
        elements.sidebar.classList.toggle("open");
    });

    elements.loadDemoButton.addEventListener("click", loadDemoData);
    elements.quickMeasurementButton.addEventListener("click", () => openMeasurementModal());
    elements.newMeasurementButton.addEventListener("click", () => openMeasurementModal());
    elements.newStationButton.addEventListener("click", () => openStationModal());
    elements.stationClassifier.addEventListener("change", toggleCustomLevels);
    elements.stationForm.addEventListener("submit", handleStationSubmit);
    elements.measurementForm.addEventListener("submit", handleMeasurementSubmit);

    elements.dashboardStationFilter.addEventListener("change", renderDashboardChart);
    elements.measurementStationFilter.addEventListener("change", renderMeasurementsTable);
    elements.measurementStatusFilter.addEventListener("change", renderMeasurementsTable);
    elements.measurementSearch.addEventListener("input", renderMeasurementsTable);
    elements.historyStationFilter.addEventListener("change", renderHistory);
    elements.historyOrderFilter.addEventListener("change", renderHistory);
    elements.exportCsvButton.addEventListener("click", exportCsv);
    elements.compareButton.addEventListener("click", renderComparison);
    elements.clearAlertsButton.addEventListener("click", clearAlerts);

    elements.stationCards.addEventListener("click", handleStationCardAction);
    elements.measurementsTable.addEventListener("click", handleMeasurementTableAction);

    elements.confirmForm.addEventListener("submit", (event) => {
        event.preventDefault();
        if (typeof state.confirmAction === "function") {
            state.confirmAction();
        }
        state.confirmAction = null;
        elements.confirmModal.close();
    });

    window.addEventListener("resize", debounce(() => {
        renderDashboardChart();
        renderHistoryChart();
    }, 160));
}

function loadStorage(key) {
    try {
        const raw = localStorage.getItem(key);
        if (!raw) return [];
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
        console.error(`Erro ao carregar ${key}:`, error);
        return [];
    }
}

function saveState() {
    localStorage.setItem(STORAGE_KEYS.stations, JSON.stringify(state.stations));
    localStorage.setItem(STORAGE_KEYS.measurements, JSON.stringify(state.measurements));
    localStorage.setItem(STORAGE_KEYS.alerts, JSON.stringify(state.alerts));
}

function navigateTo(sectionId) {
    document.querySelectorAll(".page-section").forEach((section) => {
        section.classList.toggle("active", section.id === sectionId);
    });

    document.querySelectorAll(".nav-item").forEach((item) => {
        item.classList.toggle("active", item.dataset.section === sectionId);
    });

    elements.pageTitle.textContent = SECTION_TITLES[sectionId] || "MONITORA RIONEGRO";
    elements.sidebar.classList.remove("open");

    if (sectionId === "dashboard") renderDashboardChart();
    if (sectionId === "historico") renderHistory();
    if (sectionId === "comparacao") renderComparisonPlaceholder();
}

function renderAll() {
    populateStationSelects();
    renderDashboard();
    renderStations();
    renderMeasurementsTable();
    renderHistory();
    renderAlerts();
    renderComparisonPlaceholder();
}

function populateStationSelects() {
    const currentValues = new Map();
    [
        elements.dashboardStationFilter,
        elements.measurementStationFilter,
        elements.historyStationFilter,
        elements.compareStationA,
        elements.compareStationB,
        elements.measurementStation
    ].forEach((select) => currentValues.set(select.id, select.value));

    fillSelect(elements.dashboardStationFilter, state.stations, "Todas as estações", true);
    fillSelect(elements.measurementStationFilter, state.stations, "Todas as estações", true);
    fillSelect(elements.historyStationFilter, state.stations, "Selecione uma estação", false);
    fillSelect(elements.compareStationA, state.stations, "Selecione", false);
    fillSelect(elements.compareStationB, state.stations, "Selecione", false);
    fillSelect(elements.measurementStation, state.stations, "Selecione uma estação", false);

    [
        elements.dashboardStationFilter,
        elements.measurementStationFilter,
        elements.historyStationFilter,
        elements.compareStationA,
        elements.compareStationB,
        elements.measurementStation
    ].forEach((select) => {
        const previous = currentValues.get(select.id);
        if (previous && [...select.options].some((option) => option.value === previous)) {
            select.value = previous;
        }
    });

    if (!elements.historyStationFilter.value && state.stations[0]) {
        elements.historyStationFilter.value = String(state.stations[0].id);
    }

    if (!elements.compareStationA.value && state.stations[0]) {
        elements.compareStationA.value = String(state.stations[0].id);
    }

    if (!elements.compareStationB.value && state.stations[1]) {
        elements.compareStationB.value = String(state.stations[1].id);
    }
}

function fillSelect(select, stations, placeholder, includeAll) {
    const options = [];
    options.push(`<option value="">${escapeHtml(placeholder)}</option>`);
    stations
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"))
        .forEach((station) => {
            options.push(`<option value="${station.id}">${escapeHtml(station.nome)} — ${escapeHtml(station.cidade)}</option>`);
        });
    select.innerHTML = options.join("");
    if (includeAll) select.options[0].textContent = placeholder;
}

function renderDashboard() {
    elements.statStations.textContent = state.stations.length;
    elements.statMeasurements.textContent = state.measurements.length;

    const average = state.measurements.length
        ? state.measurements.reduce((sum, item) => sum + item.nivel, 0) / state.measurements.length
        : 0;
    elements.statAverage.textContent = formatNumber(average, 2) + " m";

    const latest = getLatestMeasurement();
    resetStatusCardClasses();

    if (!latest) {
        elements.statStatus.textContent = "Sem dados";
        elements.statStatusDetail.textContent = "Cadastre uma medição";
    } else {
        const station = getStation(latest.estacaoId);
        const status = classifyMeasurement(latest, station);
        elements.statStatus.textContent = STATUS_LABELS[status];
        elements.statStatusDetail.textContent = `${station?.nome || "Estação removida"} • ${formatDate(latest.data)}`;
        elements.currentStatusCard.classList.add(`status-${status.toLowerCase()}`);
    }

    renderLatestMeasurement(latest);
    renderRecentMeasurements();
    renderDashboardChart();
}

function resetStatusCardClasses() {
    [...elements.currentStatusCard.classList]
        .filter((className) => className.startsWith("status-"))
        .forEach((className) => elements.currentStatusCard.classList.remove(className));
}

function renderLatestMeasurement(measurement) {
    if (!measurement) {
        elements.latestMeasurement.className = "latest-measurement empty-state";
        elements.latestMeasurement.innerHTML = `
            <strong>Nenhuma medição encontrada</strong>
            <span>Use o botão “Nova medição” para começar.</span>
        `;
        return;
    }

    const station = getStation(measurement.estacaoId);
    const status = classifyMeasurement(measurement, station);
    const trend = calculateTrend(measurement.estacaoId, measurement.id);

    elements.latestMeasurement.className = "latest-measurement latest-content";
    elements.latestMeasurement.innerHTML = `
        <div class="level-display">
            <span>Nível registrado</span>
            <strong>${formatNumber(measurement.nivel, 2)} m</strong>
        </div>
        <div>
            <strong>${escapeHtml(station?.nome || "Estação removida")}</strong>
            <p>${escapeHtml(station?.cidade || "Local não disponível")} • ${formatDate(measurement.data)}</p>
        </div>
        <div class="latest-meta">
            <div><span>Estado</span><strong>${statusBadge(status)}</strong></div>
            <div><span>Tendência</span><strong>${trend.label}</strong></div>
            <div><span>Chuva</span><strong>${formatNumber(measurement.chuva, 1)} mm</strong></div>
            <div><span>Temperatura</span><strong>${formatNumber(measurement.temperatura, 1)} °C</strong></div>
        </div>
        <p><strong>Recomendação:</strong> ${escapeHtml(getRecommendation(status))}</p>
    `;
}

function renderRecentMeasurements() {
    const recent = state.measurements
        .slice()
        .sort(sortByDateDesc)
        .slice(0, 6);

    elements.recentMeasurementsTable.innerHTML = recent.length
        ? recent.map((item) => measurementRow(item, false)).join("")
        : `<tr><td colspan="6" class="empty-cell">Nenhuma medição registrada.</td></tr>`;
}

function renderStations() {
    elements.stationsEmptyState.style.display = state.stations.length ? "none" : "flex";
    elements.stationCards.style.display = state.stations.length ? "grid" : "none";

    elements.stationCards.innerHTML = state.stations
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"))
        .map((station) => {
            const measurements = getMeasurementsByStation(station.id);
            const latest = measurements.slice().sort(sortByDateDesc)[0];
            const latestStatus = latest ? classifyMeasurement(latest, station) : null;

            return `
                <article class="station-card">
                    <div class="station-card-header">
                        <div>
                            <h3>${escapeHtml(station.nome)}</h3>
                            <p>${escapeHtml(station.cidade)} • ${escapeHtml(station.localizacao)}</p>
                        </div>
                        <span class="station-id">#${station.id}</span>
                    </div>
                    <div class="station-card-body">
                        <div>
                            <span>Medições</span>
                            <strong>${measurements.length}</strong>
                        </div>
                        <div>
                            <span>Último nível</span>
                            <strong>${latest ? `${formatNumber(latest.nivel, 2)} m` : "Sem dados"}</strong>
                        </div>
                        <div>
                            <span>Classificador</span>
                            <strong>${station.classificador === "PERSONALIZADO" ? "Personalizado" : "Padrão"}</strong>
                        </div>
                        <div>
                            <span>Situação</span>
                            <strong>${latestStatus ? statusBadge(latestStatus) : "Sem dados"}</strong>
                        </div>
                    </div>
                    <div class="card-actions">
                        <button class="table-action" data-action="measure" data-id="${station.id}">Medir</button>
                        <button class="table-action" data-action="edit" data-id="${station.id}">Editar</button>
                        <button class="table-action danger" data-action="delete" data-id="${station.id}">Remover</button>
                    </div>
                </article>
            `;
        })
        .join("");
}

function renderMeasurementsTable() {
    const stationId = Number(elements.measurementStationFilter.value || 0);
    const statusFilter = elements.measurementStatusFilter.value;
    const search = normalizeText(elements.measurementSearch.value);

    const filtered = state.measurements
        .filter((item) => !stationId || item.estacaoId === stationId)
        .filter((item) => {
            const station = getStation(item.estacaoId);
            return !statusFilter || classifyMeasurement(item, station) === statusFilter;
        })
        .filter((item) => {
            if (!search) return true;
            const station = getStation(item.estacaoId);
            const searchable = normalizeText(`${item.data} ${station?.nome || ""} ${station?.cidade || ""} ${item.observacao || ""}`);
            return searchable.includes(search);
        })
        .sort(sortByDateDesc);

    elements.measurementsTable.innerHTML = filtered.map((item) => measurementRow(item, true)).join("");
    elements.measurementsEmptyState.style.display = filtered.length ? "none" : "flex";
}

function measurementRow(item, includeActions) {
    const station = getStation(item.estacaoId);
    const status = classifyMeasurement(item, station);
    return `
        <tr>
            <td>${formatDate(item.data)}</td>
            <td>${escapeHtml(station?.nome || "Estação removida")}</td>
            <td><strong>${formatNumber(item.nivel, 2)} m</strong></td>
            <td>${formatNumber(item.chuva, 1)} mm</td>
            <td>${formatNumber(item.temperatura, 1)} °C</td>
            <td>${statusBadge(status)}</td>
            ${includeActions ? `
                <td>
                    <div class="table-actions">
                        <button class="table-action" data-action="edit" data-id="${item.id}">Editar</button>
                        <button class="table-action danger" data-action="delete" data-id="${item.id}">Remover</button>
                    </div>
                </td>
            ` : ""}
        </tr>
    `;
}

function renderHistory() {
    const stationId = Number(elements.historyStationFilter.value || 0);
    const station = getStation(stationId);
    const order = elements.historyOrderFilter.value;
    const recordsAsc = getMeasurementsByStation(stationId).slice().sort(sortByDateAsc);
    const records = order === "asc" ? recordsAsc : recordsAsc.slice().reverse();

    if (!station || !records.length) {
        elements.historySummary.innerHTML = `
            <div class="summary-item"><span>Estação</span><strong>${station ? escapeHtml(station.nome) : "Não selecionada"}</strong></div>
            <div class="summary-item"><span>Medições</span><strong>0</strong></div>
            <div class="summary-item"><span>Média</span><strong>0,00 m</strong></div>
            <div class="summary-item"><span>Variação total</span><strong>0,00 m</strong></div>
        `;
        elements.historyTable.innerHTML = `<tr><td colspan="7" class="empty-cell">Nenhum histórico disponível.</td></tr>`;
        clearCanvas(elements.historyChart);
        return;
    }

    const levels = recordsAsc.map((item) => item.nivel);
    const average = levels.reduce((sum, value) => sum + value, 0) / levels.length;
    const totalVariation = levels[levels.length - 1] - levels[0];

    elements.historySummary.innerHTML = `
        <div class="summary-item"><span>Estação</span><strong>${escapeHtml(station.nome)}</strong></div>
        <div class="summary-item"><span>Medições</span><strong>${records.length}</strong></div>
        <div class="summary-item"><span>Média</span><strong>${formatNumber(average, 2)} m</strong></div>
        <div class="summary-item"><span>Variação total</span><strong>${formatSigned(totalVariation)} m</strong></div>
    `;

    elements.historyTable.innerHTML = records.map((item) => {
        const originalIndex = recordsAsc.findIndex((record) => record.id === item.id);
        const previous = originalIndex > 0 ? recordsAsc[originalIndex - 1] : null;
        const variation = previous ? item.nivel - previous.nivel : null;
        const status = classifyMeasurement(item, station);
        return `
            <tr>
                <td>${formatDate(item.data)}</td>
                <td><strong>${formatNumber(item.nivel, 2)} m</strong></td>
                <td>${variation === null ? "Sem anterior" : `${formatSigned(variation)} m`}</td>
                <td>${formatNumber(item.chuva, 1)} mm</td>
                <td>${formatNumber(item.temperatura, 1)} °C</td>
                <td>${statusBadge(status)}</td>
                <td>${escapeHtml(item.observacao || "—")}</td>
            </tr>
        `;
    }).join("");

    drawLineChart(elements.historyChart, recordsAsc, station.nome);
}

function renderHistoryChart() {
    const stationId = Number(elements.historyStationFilter.value || 0);
    const station = getStation(stationId);
    const records = getMeasurementsByStation(stationId).slice().sort(sortByDateAsc);
    if (!station || !records.length) {
        clearCanvas(elements.historyChart);
        return;
    }
    drawLineChart(elements.historyChart, records, station.nome);
}

function renderDashboardChart() {
    const stationId = Number(elements.dashboardStationFilter.value || 0);
    let records = state.measurements.slice();
    let title = "Todas as estações";

    if (stationId) {
        records = records.filter((item) => item.estacaoId === stationId);
        title = getStation(stationId)?.nome || "Estação";
    }

    records.sort(sortByDateAsc);
    const limited = records.slice(-12);
    elements.chartEmptyState.style.display = limited.length ? "none" : "flex";
    elements.levelChart.style.visibility = limited.length ? "visible" : "hidden";

    if (!limited.length) {
        clearCanvas(elements.levelChart);
        return;
    }

    drawLineChart(elements.levelChart, limited, title);
}

function drawLineChart(canvas, records, title) {
    const parentWidth = canvas.parentElement.clientWidth || 800;
    const cssHeight = 330;
    const ratio = window.devicePixelRatio || 1;
    canvas.width = Math.max(600, parentWidth) * ratio;
    canvas.height = cssHeight * ratio;
    canvas.style.height = `${cssHeight}px`;
    const ctx = canvas.getContext("2d");
    ctx.setTransform(ratio, 0, 0, ratio, 0, 0);

    const width = canvas.width / ratio;
    const height = canvas.height / ratio;
    ctx.clearRect(0, 0, width, height);

    const padding = { top: 34, right: 26, bottom: 54, left: 58 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    const values = records.map((item) => item.nivel);
    let min = Math.min(...values);
    let max = Math.max(...values);
    if (min === max) {
        min -= 1;
        max += 1;
    } else {
        const rangePadding = Math.max((max - min) * 0.18, 0.5);
        min = Math.max(0, min - rangePadding);
        max += rangePadding;
    }

    ctx.font = "700 12px Segoe UI, Arial";
    ctx.fillStyle = "#15313d";
    ctx.fillText(title, padding.left, 17);

    ctx.strokeStyle = "#dce5e7";
    ctx.lineWidth = 1;
    ctx.fillStyle = "#607681";
    ctx.font = "11px Segoe UI, Arial";

    const gridLines = 5;
    for (let i = 0; i <= gridLines; i++) {
        const y = padding.top + (chartHeight / gridLines) * i;
        ctx.beginPath();
        ctx.moveTo(padding.left, y);
        ctx.lineTo(width - padding.right, y);
        ctx.stroke();

        const value = max - ((max - min) / gridLines) * i;
        ctx.fillText(`${formatNumber(value, 1)} m`, 5, y + 4);
    }

    const points = records.map((item, index) => {
        const x = records.length === 1
            ? padding.left + chartWidth / 2
            : padding.left + (chartWidth / (records.length - 1)) * index;
        const y = padding.top + ((max - item.nivel) / (max - min)) * chartHeight;
        return { x, y, item };
    });

    const gradient = ctx.createLinearGradient(0, padding.top, 0, height - padding.bottom);
    gradient.addColorStop(0, "rgba(36, 151, 172, 0.32)");
    gradient.addColorStop(1, "rgba(36, 151, 172, 0.02)");

    ctx.beginPath();
    ctx.moveTo(points[0].x, height - padding.bottom);
    points.forEach((point) => ctx.lineTo(point.x, point.y));
    ctx.lineTo(points[points.length - 1].x, height - padding.bottom);
    ctx.closePath();
    ctx.fillStyle = gradient;
    ctx.fill();

    ctx.beginPath();
    points.forEach((point, index) => {
        if (index === 0) ctx.moveTo(point.x, point.y);
        else ctx.lineTo(point.x, point.y);
    });
    ctx.strokeStyle = "#17728d";
    ctx.lineWidth = 3;
    ctx.lineJoin = "round";
    ctx.lineCap = "round";
    ctx.stroke();

    points.forEach((point, index) => {
        ctx.beginPath();
        ctx.arc(point.x, point.y, 4.5, 0, Math.PI * 2);
        ctx.fillStyle = "#ffffff";
        ctx.fill();
        ctx.strokeStyle = "#17728d";
        ctx.lineWidth = 3;
        ctx.stroke();

        if (records.length <= 8 || index % Math.ceil(records.length / 7) === 0 || index === records.length - 1) {
            ctx.save();
            ctx.translate(point.x, height - padding.bottom + 16);
            ctx.rotate(-0.35);
            ctx.fillStyle = "#607681";
            ctx.font = "10px Segoe UI, Arial";
            ctx.textAlign = "right";
            ctx.fillText(formatDateShort(point.item.data), 0, 0);
            ctx.restore();
        }
    });
}

function clearCanvas(canvas) {
    const ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

function renderAlerts() {
    const alerts = state.alerts.slice().sort((a, b) => b.criadoEm.localeCompare(a.criadoEm));
    elements.alertsEmptyState.style.display = alerts.length ? "none" : "flex";
    elements.alertsList.style.display = alerts.length ? "grid" : "none";

    elements.alertsList.innerHTML = alerts.map((alert) => `
        <article class="alert-card ${alert.estado === "EMERGENCIA" ? "emergencia" : ""}">
            <div class="alert-icon">!</div>
            <div>
                <h3>${escapeHtml(STATUS_LABELS[alert.estado])} — ${escapeHtml(alert.nomeEstacao)}</h3>
                <p>${escapeHtml(alert.mensagem)} Nível registrado: ${formatNumber(alert.nivel, 2)} m.</p>
            </div>
            <time datetime="${alert.criadoEm}">${formatDateTime(alert.criadoEm)}</time>
        </article>
    `).join("");
}

function renderComparisonPlaceholder() {
    elements.comparisonResult.className = "empty-state large";
    elements.comparisonResult.innerHTML = `
        <strong>Selecione duas estações</strong>
        <span>O sistema usará a medição mais recente de cada uma.</span>
    `;
}

function renderComparison() {
    const stationAId = Number(elements.compareStationA.value || 0);
    const stationBId = Number(elements.compareStationB.value || 0);

    if (!stationAId || !stationBId) {
        showToast("Selecione as duas estações.", "warning");
        return;
    }

    if (stationAId === stationBId) {
        showToast("Escolha estações diferentes.", "warning");
        return;
    }

    const stationA = getStation(stationAId);
    const stationB = getStation(stationBId);
    const latestA = getLatestMeasurement(stationAId);
    const latestB = getLatestMeasurement(stationBId);

    if (!latestA || !latestB) {
        elements.comparisonResult.className = "empty-state large";
        elements.comparisonResult.innerHTML = `
            <strong>Dados insuficientes</strong>
            <span>As duas estações precisam possuir pelo menos uma medição.</span>
        `;
        return;
    }

    const difference = latestA.nivel - latestB.nivel;
    const higher = difference > 0 ? stationA.nome : difference < 0 ? stationB.nome : "Níveis iguais";

    elements.comparisonResult.className = "comparison-results-grid";
    elements.comparisonResult.innerHTML = `
        ${comparisonStationCard(stationA, latestA)}
        <div class="comparison-card comparison-center">
            <div>
                <span>Diferença entre níveis</span>
                <strong>${formatNumber(Math.abs(difference), 2)} m</strong>
                <p>${difference === 0 ? "As estações possuem o mesmo nível." : `Maior nível: ${escapeHtml(higher)}`}</p>
            </div>
        </div>
        ${comparisonStationCard(stationB, latestB)}
    `;
}

function comparisonStationCard(station, measurement) {
    const status = classifyMeasurement(measurement, station);
    const trend = calculateTrend(station.id, measurement.id);
    return `
        <article class="comparison-card">
            <h3>${escapeHtml(station.nome)}</h3>
            <p>${escapeHtml(station.cidade)}</p>
            <div class="comparison-level">${formatNumber(measurement.nivel, 2)} m</div>
            ${statusBadge(status)}
            <p><strong>Data:</strong> ${formatDate(measurement.data)}</p>
            <p><strong>Tendência:</strong> ${trend.label}</p>
        </article>
    `;
}

function openStationModal(stationId = null) {
    elements.stationForm.reset();
    elements.stationFormError.textContent = "";
    elements.stationId.value = "";
    elements.stationClassifier.value = "PADRAO";
    elements.dryLimit.value = DEFAULT_LIMITS.dry;
    elements.attentionLimit.value = DEFAULT_LIMITS.attention;
    elements.floodLimit.value = DEFAULT_LIMITS.flood;
    elements.emergencyLimit.value = DEFAULT_LIMITS.emergency;

    if (stationId) {
        const station = getStation(stationId);
        if (!station) return;
        elements.stationModalTitle.textContent = "EDITAR ESTAÇÃO";
        elements.stationId.value = station.id;
        elements.stationName.value = station.nome;
        elements.stationCity.value = station.cidade;
        elements.stationLocation.value = station.localizacao;
        elements.stationClassifier.value = station.classificador;
        const limits = station.limites || DEFAULT_LIMITS;
        elements.dryLimit.value = limits.dry;
        elements.attentionLimit.value = limits.attention;
        elements.floodLimit.value = limits.flood;
        elements.emergencyLimit.value = limits.emergency;
    } else {
        elements.stationModalTitle.textContent = "CADASTRAR ESTAÇÃO";
    }

    toggleCustomLevels();
    elements.stationModal.showModal();
    setTimeout(() => elements.stationName.focus(), 50);
}

function toggleCustomLevels() {
    elements.customLevels.classList.toggle("visible", elements.stationClassifier.value === "PERSONALIZADO");
}

function handleStationSubmit(event) {
    event.preventDefault();
    elements.stationFormError.textContent = "";

    try {
        const id = Number(elements.stationId.value || 0);
        const nome = requiredText(elements.stationName.value, "Informe o nome da estação.");
        const cidade = requiredText(elements.stationCity.value, "Informe a cidade.");
        const localizacao = requiredText(elements.stationLocation.value, "Informe a localização.");
        const classificador = elements.stationClassifier.value;
        const limites = classificador === "PERSONALIZADO"
            ? validateLimits({
                dry: Number(elements.dryLimit.value),
                attention: Number(elements.attentionLimit.value),
                flood: Number(elements.floodLimit.value),
                emergency: Number(elements.emergencyLimit.value)
            })
            : { ...DEFAULT_LIMITS };

        const duplicate = state.stations.some((station) =>
            station.id !== id && normalizeText(station.nome) === normalizeText(nome)
        );
        if (duplicate) throw new Error("Já existe uma estação com esse nome.");

        if (id) {
            const station = getStation(id);
            Object.assign(station, { nome, cidade, localizacao, classificador, limites });
            showToast("Estação atualizada com sucesso.", "success");
        } else {
            state.stations.push({
                id: nextId(state.stations),
                nome,
                cidade,
                localizacao,
                classificador,
                limites
            });
            showToast("Estação cadastrada com sucesso.", "success");
        }

        saveState();
        elements.stationModal.close();
        renderAll();
    } catch (error) {
        elements.stationFormError.textContent = error.message;
    }
}

function openMeasurementModal(measurementId = null, preselectedStationId = null) {
    if (!state.stations.length) {
        showToast("Cadastre uma estação antes de registrar medições.", "warning");
        navigateTo("estacoes");
        return;
    }

    elements.measurementForm.reset();
    elements.measurementFormError.textContent = "";
    elements.measurementId.value = "";
    setTodayAsDefault();
    populateStationSelects();

    if (measurementId) {
        const measurement = state.measurements.find((item) => item.id === measurementId);
        if (!measurement) return;
        elements.measurementModalTitle.textContent = "EDITAR MEDIÇÃO";
        elements.measurementId.value = measurement.id;
        elements.measurementStation.value = String(measurement.estacaoId);
        elements.measurementDate.value = measurement.data;
        elements.measurementLevel.value = measurement.nivel;
        elements.measurementRain.value = measurement.chuva;
        elements.measurementTemperature.value = measurement.temperatura;
        elements.measurementObservation.value = measurement.observacao || "";
    } else {
        elements.measurementModalTitle.textContent = "REGISTRAR MEDIÇÃO";
        elements.measurementStation.value = String(preselectedStationId || state.stations[0].id);
    }

    elements.measurementModal.showModal();
    setTimeout(() => elements.measurementStation.focus(), 50);
}

function handleMeasurementSubmit(event) {
    event.preventDefault();
    elements.measurementFormError.textContent = "";

    try {
        const id = Number(elements.measurementId.value || 0);
        const estacaoId = Number(elements.measurementStation.value);
        const data = elements.measurementDate.value;
        const nivel = parseNonNegative(elements.measurementLevel.value, "Informe um nível válido.");
        const chuva = parseNonNegative(elements.measurementRain.value, "Informe uma quantidade de chuva válida.");
        const temperatura = Number(elements.measurementTemperature.value);
        const observacao = elements.measurementObservation.value.trim();

        if (!getStation(estacaoId)) throw new Error("Selecione uma estação válida.");
        if (!data) throw new Error("Informe a data da medição.");
        if (!Number.isFinite(temperatura) || temperatura < -20 || temperatura > 60) {
            throw new Error("Informe uma temperatura entre -20 °C e 60 °C.");
        }

        const duplicate = state.measurements.some((item) =>
            item.id !== id && item.estacaoId === estacaoId && item.data === data
        );
        if (duplicate) throw new Error("Já existe uma medição dessa estação na data informada.");

        let measurement;
        if (id) {
            measurement = state.measurements.find((item) => item.id === id);
            Object.assign(measurement, { estacaoId, data, nivel, chuva, temperatura, observacao });
            removeAlertsByMeasurement(id);
            showToast("Medição atualizada com sucesso.", "success");
        } else {
            measurement = {
                id: nextId(state.measurements),
                estacaoId,
                data,
                nivel,
                chuva,
                temperatura,
                observacao,
                criadoEm: new Date().toISOString()
            };
            state.measurements.push(measurement);
            showToast("Medição registrada com sucesso.", "success");
        }

        generateAlertIfNeeded(measurement);
        saveState();
        elements.measurementModal.close();
        renderAll();
    } catch (error) {
        elements.measurementFormError.textContent = error.message;
    }
}

function handleStationCardAction(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const id = Number(button.dataset.id);
    const action = button.dataset.action;

    if (action === "edit") openStationModal(id);
    if (action === "measure") openMeasurementModal(null, id);
    if (action === "delete") confirmDeleteStation(id);
}

function handleMeasurementTableAction(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const id = Number(button.dataset.id);
    if (button.dataset.action === "edit") openMeasurementModal(id);
    if (button.dataset.action === "delete") confirmDeleteMeasurement(id);
}

function confirmDeleteStation(id) {
    const station = getStation(id);
    if (!station) return;
    const count = getMeasurementsByStation(id).length;
    openConfirm(
        "Remover estação?",
        `A estação “${station.nome}” e suas ${count} medição(ões) serão removidas.`,
        () => {
            const measurementIds = state.measurements.filter((item) => item.estacaoId === id).map((item) => item.id);
            state.stations = state.stations.filter((item) => item.id !== id);
            state.measurements = state.measurements.filter((item) => item.estacaoId !== id);
            state.alerts = state.alerts.filter((item) => !measurementIds.includes(item.medicaoId));
            saveState();
            renderAll();
            showToast("Estação removida.", "success");
        }
    );
}

function confirmDeleteMeasurement(id) {
    const measurement = state.measurements.find((item) => item.id === id);
    if (!measurement) return;
    openConfirm(
        "Remover medição?",
        `A medição de ${formatDate(measurement.data)} será excluída permanentemente.`,
        () => {
            state.measurements = state.measurements.filter((item) => item.id !== id);
            removeAlertsByMeasurement(id);
            saveState();
            renderAll();
            showToast("Medição removida.", "success");
        }
    );
}

function openConfirm(title, message, action) {
    elements.confirmTitle.textContent = title;
    elements.confirmMessage.textContent = message;
    state.confirmAction = action;
    elements.confirmModal.showModal();
}

function generateAlertIfNeeded(measurement) {
    const station = getStation(measurement.estacaoId);
    if (!station) return;
    const status = classifyMeasurement(measurement, station);
    if (!["ATENCAO", "CHEIA", "EMERGENCIA", "SECA"].includes(status)) return;

    state.alerts.push({
        id: nextId(state.alerts),
        medicaoId: measurement.id,
        estacaoId: station.id,
        nomeEstacao: station.nome,
        estado: status,
        nivel: measurement.nivel,
        mensagem: getRecommendation(status),
        criadoEm: new Date().toISOString()
    });
}

function removeAlertsByMeasurement(measurementId) {
    state.alerts = state.alerts.filter((item) => item.medicaoId !== measurementId);
}

function clearAlerts() {
    if (!state.alerts.length) {
        showToast("Não há alertas para limpar.", "warning");
        return;
    }
    openConfirm(
        "Limpar todos os alertas?",
        "As medições serão mantidas; apenas a lista de alertas será removida.",
        () => {
            state.alerts = [];
            saveState();
            renderAlerts();
            showToast("Alertas removidos.", "success");
        }
    );
}

function classifyMeasurement(measurement, station) {
    const limits = station?.limites || DEFAULT_LIMITS;
    const level = Number(measurement.nivel);
    if (level < limits.dry) return "SECA";
    if (level < limits.attention) return "NORMAL";
    if (level < limits.flood) return "ATENCAO";
    if (level < limits.emergency) return "CHEIA";
    return "EMERGENCIA";
}

function getRecommendation(status) {
    const recommendations = {
        SECA: "Acompanhar possíveis impactos na navegação, no abastecimento e no acesso às comunidades.",
        NORMAL: "Manter o acompanhamento periódico dos níveis do rio.",
        ATENCAO: "Aumentar a frequência das medições e observar áreas sensíveis.",
        CHEIA: "Reforçar o monitoramento e preparar medidas preventivas.",
        EMERGENCIA: "Acionar equipes responsáveis e comunicar áreas com risco de inundação."
    };
    return recommendations[status] || "Continuar o monitoramento.";
}

function calculateTrend(stationId, measurementId) {
    const records = getMeasurementsByStation(stationId).slice().sort(sortByDateAsc);
    const index = records.findIndex((item) => item.id === measurementId);
    if (index <= 0) return { label: "Sem medição anterior", value: null };
    const value = records[index].nivel - records[index - 1].nivel;
    if (value > 0) return { label: `Subindo (${formatSigned(value)} m)`, value };
    if (value < 0) return { label: `Baixando (${formatSigned(value)} m)`, value };
    return { label: "Estável (0,00 m)", value };
}

function getStation(id) {
    return state.stations.find((item) => item.id === Number(id));
}

function getMeasurementsByStation(stationId) {
    return state.measurements.filter((item) => item.estacaoId === Number(stationId));
}

function getLatestMeasurement(stationId = null) {
    const records = stationId
        ? getMeasurementsByStation(stationId)
        : state.measurements.slice();
    return records.sort(sortByDateDesc)[0] || null;
}

function loadDemoData() {
    if (state.stations.length || state.measurements.length) {
        openConfirm(
            "Carregar dados demonstrativos?",
            "Os dados atuais serão substituídos pelos exemplos do projeto.",
            () => applyDemoData()
        );
        return;
    }
    applyDemoData();
}

function applyDemoData() {
    state.stations = [
        {
            id: 1,
            nome: "Porto de Manaus",
            cidade: "Manaus",
            localizacao: "Centro, margem esquerda do Rio Negro",
            classificador: "PADRAO",
            limites: { ...DEFAULT_LIMITS }
        },
        {
            id: 2,
            nome: "Estação Novo Airão",
            cidade: "Novo Airão",
            localizacao: "Orla municipal",
            classificador: "PERSONALIZADO",
            limites: { dry: 14, attention: 25.5, flood: 27.5, emergency: 29 }
        },
        {
            id: 3,
            nome: "Estação Barcelos",
            cidade: "Barcelos",
            localizacao: "Área portuária",
            classificador: "PADRAO",
            limites: { ...DEFAULT_LIMITS }
        }
    ];

    const days = [
        ["2026-07-20", 24.5, 22, 30],
        ["2026-07-21", 25.2, 35, 29.5],
        ["2026-07-22", 26.4, 48, 29],
        ["2026-07-23", 27.1, 55, 28.5],
        ["2026-07-24", 28.2, 65, 29]
    ];

    state.measurements = [];
    let id = 1;
    days.forEach(([data, nivel, chuva, temperatura]) => {
        state.measurements.push({ id: id++, estacaoId: 1, data, nivel, chuva, temperatura, observacao: "Medição demonstrativa", criadoEm: new Date().toISOString() });
        state.measurements.push({ id: id++, estacaoId: 2, data, nivel: nivel - 1.3, chuva: Math.max(0, chuva - 8), temperatura: temperatura + 0.6, observacao: "Medição demonstrativa", criadoEm: new Date().toISOString() });
        state.measurements.push({ id: id++, estacaoId: 3, data, nivel: nivel - 2.1, chuva: Math.max(0, chuva - 13), temperatura: temperatura + 1, observacao: "Medição demonstrativa", criadoEm: new Date().toISOString() });
    });

    state.alerts = [];
    state.measurements.forEach(generateAlertIfNeeded);
    saveState();
    renderAll();
    showToast("Dados demonstrativos carregados.", "success");
}

function exportCsv() {
    if (!state.measurements.length) {
        showToast("Não existem medições para exportar.", "warning");
        return;
    }

    const header = [
        "ID", "Estacao", "Cidade", "Data", "Nivel_m", "Chuva_24h_mm",
        "Temperatura_C", "Estado", "Observacao"
    ];

    const rows = state.measurements
        .slice()
        .sort(sortByDateAsc)
        .map((item) => {
            const station = getStation(item.estacaoId);
            return [
                item.id,
                station?.nome || "Estação removida",
                station?.cidade || "",
                item.data,
                item.nivel.toFixed(2),
                item.chuva.toFixed(2),
                item.temperatura.toFixed(1),
                STATUS_LABELS[classifyMeasurement(item, station)],
                item.observacao || ""
            ];
        });

    const csv = [header, ...rows]
        .map((row) => row.map(csvEscape).join(";"))
        .join("\n");

    const blob = new Blob(["\uFEFF" + csv], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "historico_monitora_rionegro.csv";
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    showToast("Arquivo CSV exportado.", "success");
}

function setTodayAsDefault() {
    if (!elements.measurementDate) return;
    elements.measurementDate.value = new Date().toISOString().slice(0, 10);
}

function validateLimits(limits) {
    if (Object.values(limits).some((value) => !Number.isFinite(value) || value < 0)) {
        throw new Error("As faixas devem possuir números válidos e não negativos.");
    }
    if (!(limits.dry < limits.attention && limits.attention < limits.flood && limits.flood < limits.emergency)) {
        throw new Error("Os limites devem seguir a ordem: seca < atenção < cheia < emergência.");
    }
    return limits;
}

function requiredText(value, message) {
    const text = String(value || "").trim();
    if (!text) throw new Error(message);
    return text;
}

function parseNonNegative(value, message) {
    const number = Number(value);
    if (!Number.isFinite(number) || number < 0) throw new Error(message);
    return number;
}

function nextId(items) {
    return items.length ? Math.max(...items.map((item) => Number(item.id) || 0)) + 1 : 1;
}

function statusBadge(status) {
    return `<span class="status-badge badge-${status.toLowerCase()}">${STATUS_LABELS[status]}</span>`;
}

function sortByDateAsc(a, b) {
    return a.data.localeCompare(b.data) || a.id - b.id;
}

function sortByDateDesc(a, b) {
    return b.data.localeCompare(a.data) || b.id - a.id;
}

function formatNumber(value, decimals = 2) {
    return Number(value || 0).toLocaleString("pt-BR", {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals
    });
}

function formatSigned(value) {
    const prefix = value > 0 ? "+" : "";
    return prefix + formatNumber(value, 2);
}

function formatDate(value) {
    if (!value) return "—";
    const [year, month, day] = value.split("-");
    return `${day}/${month}/${year}`;
}

function formatDateShort(value) {
    if (!value) return "—";
    const [, month, day] = value.split("-");
    return `${day}/${month}`;
}

function formatDateTime(value) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "—";
    return date.toLocaleString("pt-BR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function csvEscape(value) {
    const text = String(value ?? "").replace(/\r?\n/g, " ");
    return `"${text.replace(/"/g, '""')}"`;
}

function normalizeText(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
}

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function showToast(message, type = "default") {
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.textContent = message;
    elements.toastContainer.appendChild(toast);
    setTimeout(() => toast.remove(), 3400);
}

function debounce(callback, delay) {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => callback(...args), delay);
    };
}
