package com.example.agrifleettask5.service;

import com.example.agrifleettask5.model.FarmOpportunity;
import com.example.agrifleettask5.model.FarmSelectionRequest;
import com.example.agrifleettask5.model.FarmSelectionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class FarmSelectionService {
        private static final String CORE_SERVICE_URL = "http://localhost:8080/api/v1";
        private final RestTemplate restTemplate = new RestTemplate();

        public List<FarmOpportunity> getAvailableFarmsFromCore() {
                BookingDTO[] bookings = restTemplate.getForObject(CORE_SERVICE_URL + "/bookings", BookingDTO[].class);
                if (bookings == null || bookings.length == 0) {
                        return List.of();
                }

                return Arrays.stream(bookings)
                                .filter(Objects::nonNull)
                                .filter(booking -> booking.bookingId != null && booking.farmLat != null
                                                && booking.farmLng != null)
                                .map(this::mapToFarmOpportunity)
                                .filter(Objects::nonNull)
                                .toList();
        }

        public FarmSelectionResponse select(FarmSelectionRequest request) {
                if (request == null) {
                        throw new IllegalArgumentException("request is required");
                }
                if (request.availableFarms() == null || request.availableFarms().isEmpty()) {
                        return new FarmSelectionResponse(List.of(), 0.0, 0.0, 0.0,
                                        "AG19_WEIGHTED_SCORE", "maximize acreage and booking value");
                }

                int maxFarms = request.maxFarms() == null || request.maxFarms() <= 0
                                ? request.availableFarms().size()
                                : Math.min(request.maxFarms(), request.availableFarms().size());
                double acreageWeight = request.acreageWeight() == null ? 0.5 : request.acreageWeight();
                double bookingValueWeight = request.bookingValueWeight() == null ? 0.5 : request.bookingValueWeight();

                if (acreageWeight < 0 || bookingValueWeight < 0) {
                        throw new IllegalArgumentException("weights must be non-negative");
                }
                double totalWeight = acreageWeight + bookingValueWeight;
                if (totalWeight <= 0) {
                        throw new IllegalArgumentException("weight sum must be greater than zero");
                }

                double maxAcreage = request.availableFarms().stream()
                                .mapToDouble(FarmOpportunity::acreageHa)
                                .max()
                                .orElse(0.0);
                double maxBookingValue = request.availableFarms().stream()
                                .mapToDouble(FarmOpportunity::bookingValue)
                                .max()
                                .orElse(0.0);

                List<FarmOpportunity> ranked = request.availableFarms().stream()
                                .sorted(Comparator
                                                .comparingDouble((FarmOpportunity farm) -> score(farm, acreageWeight,
                                                                bookingValueWeight,
                                                                maxAcreage, maxBookingValue))
                                                .reversed()
                                                .thenComparingDouble(FarmOpportunity::acreageHa).reversed()
                                                .thenComparingDouble(FarmOpportunity::bookingValue).reversed()
                                                .thenComparing(FarmOpportunity::name))
                                .limit(maxFarms)
                                .toList();

                double totalAcreage = ranked.stream().mapToDouble(FarmOpportunity::acreageHa).sum();
                double totalBookingValue = ranked.stream().mapToDouble(FarmOpportunity::bookingValue).sum();
                double objectiveScore = ranked.stream()
                                .mapToDouble(farm -> score(farm, acreageWeight, bookingValueWeight, maxAcreage,
                                                maxBookingValue))
                                .average()
                                .orElse(0.0);

                return new FarmSelectionResponse(ranked, totalAcreage, totalBookingValue,
                                objectiveScore, "AG19_WEIGHTED_SCORE",
                                "maximize acreage and booking value");
        }

        private double score(FarmOpportunity farm, double acreageWeight, double bookingValueWeight,
                        double maxAcreage, double maxBookingValue) {
                double normalizedAcreage = maxAcreage == 0 ? 0.0 : farm.acreageHa() / maxAcreage;
                double normalizedBookingValue = maxBookingValue == 0 ? 0.0 : farm.bookingValue() / maxBookingValue;
                return ((acreageWeight / (acreageWeight + bookingValueWeight)) * normalizedAcreage)
                                + ((bookingValueWeight / (acreageWeight + bookingValueWeight))
                                                * normalizedBookingValue);
        }

        private FarmOpportunity mapToFarmOpportunity(BookingDTO booking) {
                Double acreage = booking.acreage != null ? booking.acreage : 0.0;
                Double bookingValue = booking.totalValue != null ? booking.totalValue : acreage * 250.0;
                String cropType = booking.cropType != null ? booking.cropType : "UNKNOWN";

                if (acreage <= 0 || bookingValue <= 0) {
                        return null;
                }

                return new FarmOpportunity(
                                booking.bookingId,
                                "Booking #" + booking.bookingId,
                                acreage,
                                bookingValue,
                                cropType);
        }

        public static class BookingDTO {
                private Long bookingId;
                private Double acreage;
                private Double totalValue;
                private String cropType;
                private Double farmLat;
                private Double farmLng;

                public Long getBookingId() {
                        return bookingId;
                }

                public void setBookingId(Long bookingId) {
                        this.bookingId = bookingId;
                }

                public Double getAcreage() {
                        return acreage;
                }

                public void setAcreage(Double acreage) {
                        this.acreage = acreage;
                }

                public Double getTotalValue() {
                        return totalValue;
                }

                public void setTotalValue(Double totalValue) {
                        this.totalValue = totalValue;
                }

                public String getCropType() {
                        return cropType;
                }

                public void setCropType(String cropType) {
                        this.cropType = cropType;
                }

                public Double getFarmLat() {
                        return farmLat;
                }

                public void setFarmLat(Double farmLat) {
                        this.farmLat = farmLat;
                }

                public Double getFarmLng() {
                        return farmLng;
                }

                public void setFarmLng(Double farmLng) {
                        this.farmLng = farmLng;
                }
        }
}
