package com.mashreq.transfercoreservice.limits;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitServiceImpl implements LimitService {

    private final LimitDTO.LimitPackageDefaultMapper limitPackageDefaultMapper;
    private final LimitPackageDefaultRepository limitPackageDefaultRepository;

    @Override
    public Optional<LimitDTO> getDefaultLPByBeneficiaryTypeAndSegmentAndCountry(String beneficiaryType, Long segmentId, Long countryId) {

        LimitPackageDefault limitPackageDefault =
                limitPackageDefaultRepository.findByBeneficiaryTypeCodeAndSegmentIdAndCountryId(beneficiaryType, segmentId, countryId );
        LimitDTO limitDTO = limitPackageDefaultMapper.limitDtoFromEntity(limitPackageDefault);
        return Optional.ofNullable(limitDTO);
    }

    @Override
    public Optional<LimitDTO> getUserLPByUserId() {
        return Optional.empty();
    }

    @Override
    public Optional<LimitDTO> getCustomerLPByCif() {
        return Optional.empty();
    }

}
