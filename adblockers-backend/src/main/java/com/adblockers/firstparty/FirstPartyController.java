package com.adblockers.firstparty;

import com.adblockers.parser.LongCsvReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.omg.CORBA.Request;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@RestController
@RequestMapping("/firstparty")
public class FirstPartyController {

    private FirstPartyRepository firstPartyRepository;

    @RequestMapping(value = {"/extract/{fileName}"}, method = RequestMethod.GET)
    public void extractFirstParties(
            @RequestParam String fileName,
            @RequestParam(value = "min", required = false) Long min,
            @RequestParam(value = "max", required = false) Long max
    ) {
        this.firstPartyRepository.deleteAll();
        LongCsvReader<FirstParty> longCsvReader = new LongCsvReader(fileName);
        longCsvReader.setFilter(firstParty -> firstParty.getRank() < 200);
        longCsvReader.setLineParser(line -> new FirstParty(Integer.parseInt(line[0]), "http://www." + line[1]));
        longCsvReader.setMin(min);
        longCsvReader.setMax(max);
        this.firstPartyRepository.save(longCsvReader.read());
    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public void deleteFirstParties() {
        this.firstPartyRepository.deleteAll();
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long countFirstParties() {
        return this.firstPartyRepository.count();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Collection<?> getFirstParties() {
        return this.firstPartyRepository.findAll()
                .stream()
                .map(firstParty -> ImmutableMap.<String, Object>builder()
                        .put("rank", firstParty.getRank())
                        .put("url", firstParty.getUrl().getHost().toString())
                        .build())
                .collect(Collectors.toList());
    }

    @Autowired
    public void setFirstPartyRepository(FirstPartyRepository firstPartyRepository) {
        this.firstPartyRepository = firstPartyRepository;
    }
}
