Database=jdbc:oracle:thin:@${org.cougaar.database}
Driver = oracle.jdbc.driver.OracleDriver
Username = ${icis.database.user}
Password = ${icis.database.password}
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

headerQuery=select commodity, nsn, nomenclature, ui, ssc, price, icc, alt, plt, pcm, boq, diq, iaq, nso, qfd, rop, owrmrp, weight, cube, aac, slq from header where NSN = :nsns
assetsQuery=select nsn, ric, purpose, condition, iaq from assets where NSN = :nsns
nomen=select nomenclature from header where NSN = :nsns	
cost=select price from header where NSN = :nsns
volume=select cube from header where NSN = :nsns
weight=select weight from header where NSN = :nsns
classIXData=select nomenclature, ui, price, cube, weight from header where NSN = :nsns 
classVData=select nomenclature, weight, ccc from ammo_characteristics where DODIC = :nsns
ui=select ui from header where NSN = :nsns
# MEI
#
meiQuery=select NOMENCLATURE from AGGREGATED_MEI_NOMENCLATURE where MEI = :nsns and SERVICE = :service
# ARMY
#
ConsumableArmyNSN=select MEI_NSN, PART_NSN, OPTEMPO, DCR from ARMY_SPARES_DCR_BY_OPTEMPO where MEI_NSN = :nsns and OPTEMPO = 'HIGH' order by DCR desc
#BulkPOLArmyNSN=select NSN, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from ARMY_FUELS_DCR_BY_OPTEMPO where NSN = :nsns order by GALLONS_PER_DAY desc
BulkPOLArmyNSN=select NSN, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from ALP_MEI_FUEL where NSN = :nsns order by GALLONS_PER_DAY desc
AmmunitionArmyNSN=select MEI_NSN, DODIC, OPTEMPO, TONS_PER_DAY from alp_mei_dodic_2_view where MEI_NSN = :nsns order by TONS_PER_DAY desc
# AirForce
#
ConsumableAirforceMDS=select MDS, NSN, OPTEMPO, DEMANDS_PER_DAY from airforce_spares_dcr_by_optempo where MDS = :nsns order by DEMANDS_PER_DAY
BulkPOLAirforceMDS=select MDS, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from AIRFORCE_FUELS_DCR_BY_OPTEMPO where MDS = :nsns order by GALLONS_PER_DAY
# Marine
# 
ConsumableMarineTAMCN=select TAMCN, PART_NSN, OPTEMPO, DCR from MCGRD_SPARES_DCR_BY_OPTEMPO where TAMCN = :nsns order by DCR
ConsumableMarineNSN=select MEI_NSN, PART_NSN, OPTEMPO, DCR from MCGRD_SPARES_DCR_BY_OPTEMPO where MEI_NSN = :nsns order by DCR
ConsumableMarineMDS=select MDS,NSN, OPTEMPO, DEMANDS_PER_DAY from USMCAIR_SPARES_DCR_BY_OPTEMPO where MDS = :nsns order by DEMANDS_PER_DAY
BulkPOLMarineTAMCN=select TAMCN, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from MARINE_GROUND_FUELS_DCR_BY_OP where TAMCN = :nsns order by GALLONS_PER_DAY
BulkPOLMarineMDS=select MDS, FUEL_NSN, OPTEMPO, GALLONS_PER_DAY from MARINE_AIR_FUELS_DCR_BY_OP where MDS = :nsns order by GALLONS_PER_DAY
# Navy
#
ConsumableNavyMEI=select MEI_ID, NSN, OPTEMPO, DCR from NAVY_SPARES_DCR_BY_OPTEMPO where MEI_ID = :nsns order by DCR
ConsumableNavyMDS=select MDS, NSN, OPTEMPO, DEMANDS_PER_DAY from NAVYAIR_SPARES_DCR_BY_OPTEMPO where MDS = :nsns order by DEMANDS_PER_DAY
# Prototype & Property Provider
#
#%org.cougaar.domain.glm.ldm.GLMPrototypeProvider
#%org.cougaar.domain.glm.ldm.GLMPropertyProvider
